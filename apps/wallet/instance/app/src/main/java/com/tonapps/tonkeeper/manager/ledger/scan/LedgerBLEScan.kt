package com.tonapps.tonkeeper.manager.ledger.scan

import android.content.Context
import android.util.Log
import com.ledger.live.ble.BleManagerFactory
import com.ledger.live.ble.model.BleDeviceModel
import com.tonapps.ledger.ble.BleTransport
import com.tonapps.ledger.devices.Devices
import com.tonapps.ledger.ton.TonTransport
import com.tonapps.tonkeeper.manager.ledger.LedgerState
import com.tonapps.tonkeeper.manager.ledger.connection.LedgerBLEConnection
import com.tonapps.tonkeeper.manager.ledger.device.LedgerDevice
import com.tonapps.tonkeeper.manager.ledger.safeIsAppOpened
import com.tonapps.tonkeeper.manager.ledger.safeStopScanning
import com.tonapps.tonkeeper.manager.ledger.waitForAppOpen
import com.tonapps.tonkeeper.ui.screen.ledger.steps.ConnectedDevice
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.minutes

class LedgerBLEScan(context: Context): LedgerScan(context) {

    private val bleManager = BleManagerFactory.newInstance(context)

    override fun start() {
        bleManager.startScanning { devices ->
            setDevices(devices.map {
                LedgerDevice.BLE(it)
            })
            bleManager.safeStopScanning()
        }
    }

    override fun stop() {
        bleManager.safeStopScanning()
    }

    fun connectionFlow() = flow {
        val device = findDevice()
        emit(LedgerState.Found)

        val connectedDevice = connectWithRetry(device)

        emit(LedgerState.Connected)

        val tonTransport = createTonTransport()

        emit(LedgerState.TonAppOpened)
    }.flowOn(Dispatchers.IO)

    private suspend fun createTonTransport(): TonTransport {
        val tonTransport = TonTransport(BleTransport(bleManager))
        tonTransport.waitForAppOpen()
        Log.d("LedgerBLEScan", "app is open")
        return tonTransport
    }

    private suspend fun findDevice() = suspendCancellableCoroutine {
        bleManager.startScanning { devices ->
            val device = devices.first()
            bleManager.safeStopScanning()
            it.resume(device)
        }
    }

    private suspend fun connect(device: BleDeviceModel) = suspendCancellableCoroutine { continuation ->
        bleManager.connect(
            address = device.id,
            onConnectError = { error ->
                continuation.cancel(Throwable(error.message))
            },
            onConnectSuccess = { connectedDevice ->
                continuation.resume(ConnectedDevice(
                    deviceId = connectedDevice.id,
                    model = Devices.fromServiceUuid(connectedDevice.serviceId!!)
                ))
            }
        )
    }

    private suspend fun connectWithRetry(
        device: BleDeviceModel,
        attempt: Int = 0,
    ): ConnectedDevice {
        try {
            return connect(device)
        } catch (e: Throwable) {
            if (e is CancellationException || attempt > 3) {
                throw e
            }
            delay(1000)
            return connectWithRetry(device, attempt + 1)
        }
    }

}