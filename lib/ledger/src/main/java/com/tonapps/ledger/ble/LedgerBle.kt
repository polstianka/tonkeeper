package com.tonapps.ledger.ble

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.ledger.live.ble.BleManagerFactory
import com.ledger.live.ble.model.BleDeviceModel
import com.tonapps.ledger.devices.Devices
import com.tonapps.ledger.ton.TonTransport
import com.tonapps.ledger.transport.TransportStatusException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LedgerBle(private val context: Context) {
    private val bleManager = BleManagerFactory.newInstance(context)

    fun deviceListener(callback: (device: BleDeviceModel) -> Unit) {
        bleManager.startScanning {
            callback(it.first())
            try {
                bleManager.stopScanning()
            } catch (ignored: Exception) {}
        }
    }

    fun close() {
        try {
            bleManager.stopScanning()
        } catch (ignored: Exception) {}
    }

    suspend fun connectDevice(device: BleDeviceModel): ConnectedDevice = suspendCancellableCoroutine { continuation ->
        bleManager.connect(
            address = device.id,
            onConnectError = {
                continuation.cancel(Throwable(it.message))
            },
            onConnectSuccess = {
                val connected = ConnectedDevice(device.id, Devices.fromServiceUuid(it.serviceId!!))
                continuation.resume(connected)
            }
        )
    }

    suspend fun createTonTransfer(
        device: ConnectedDevice,
        attempt: Int = 0
    ): TonTransport {
        val transport = BleTransport(bleManager, device)
        val tonTransport = TonTransport(transport)
        if (!tonTransport.isLocked()) {
            return tonTransport
        }
        if (attempt >= 30) {
            throw TransportStatusException.LockedDevice()
        }
        delay(2000)
        return createTonTransfer(device, attempt + 1)
    }
}