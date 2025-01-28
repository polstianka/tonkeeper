package com.tonapps.tonkeeper.ui.screen.external.ledger

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.ledger.live.ble.model.BleDeviceModel
import com.tonapps.extensions.bestMessage
import com.tonapps.ledger.ble.LedgerBle
import com.tonapps.ledger.ton.TonTransport
import com.tonapps.ledger.usb.LedgerUsb
import com.tonapps.tonkeeper.extensions.isVersionLowerThan
import com.tonapps.tonkeeper.ui.base.BaseWalletVM
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LedgerConnectViewModel(
    app: Application
): BaseWalletVM(app) {

    private val _connectionTypeFlow = MutableStateFlow(Ledger.ConnectionType.NONE)
    val connectionTypeFlow = _connectionTypeFlow.asStateFlow()

    private val _stateFlow = MutableStateFlow<Ledger.State>(Ledger.State.Default)
    val stateFlow = _stateFlow.asStateFlow()

    val tasksFlow = connectionTypeFlow.map {
        val baseTasks = if (it == Ledger.ConnectionType.USB) baseUsbTasks else baseBleTasks
        baseTasks.toList()
    }

    private val ledgerUsb: LedgerUsb by lazy { LedgerUsb(app) }
    private val ledgerBle: LedgerBle by lazy { LedgerBle(app) }

    private var deviceFlowJob: Job? = null
    private var currentTransport: TonTransport? = null

    fun setConnectionType(connectionType: Ledger.ConnectionType) {
        if (_connectionTypeFlow.value != connectionType) {
            _connectionTypeFlow.value = connectionType
            deviceFlowJob?.cancel()
            if (connectionType == Ledger.ConnectionType.USB) {
                startUSBDeviceListener()
            } else if (connectionType == Ledger.ConnectionType.BLE) {
                startBLEDeviceListener()
            }
        }
    }

    private fun startBLEDeviceListener() {
        Log.d(LOG_TAG, "startBLEDeviceListener")
        currentTransport = null
        _stateFlow.value = Ledger.State.WaitingForConnection
        ledgerBle.deviceListener { device ->
            setBleDevice(device)
        }
    }

    private fun startUSBDeviceListener() {
        Log.d(LOG_TAG, "startUSBDeviceListener")
        currentTransport = null
        _stateFlow.value = Ledger.State.WaitingForConnection
        deviceFlowJob = ledgerUsb.deviceFlow()
            .map(ledgerUsb::connectDevice)
            .map(ledgerUsb::createTonTransport)
            .map(::setTonTransport)
            .catch { setFailed(it) }
            .launchIn(viewModelScope)
    }

    private suspend fun setFailed(e: Throwable) {
        Log.d(LOG_TAG, "failed", e)
        if (e !is CancellationException) {
            toast(e.bestMessage)
            _stateFlow.value = Ledger.State.Idle
            finish()
        }
    }

    private fun setBleDevice(device: BleDeviceModel) {
        viewModelScope.launch {
            try {
                val connected = ledgerBle.connectDevice(device)
                val tonTransport = ledgerBle.createTonTransfer(connected)
                setTonTransport(tonTransport)
            } catch (e: Throwable) {
                setFailed(e)
            }
        }
    }

    private suspend fun setTonTransport(transport: TonTransport) = withContext(Dispatchers.IO) {
        Log.d(LOG_TAG, "setTonTransport: $transport")
        _stateFlow.value = Ledger.State.WaitingAppTON
        val currentApp = transport.getCurrentApp()
        Log.d(LOG_TAG, "currentApp: $currentApp")
        if (currentApp.name == "BOLOS") {
            Log.d(LOG_TAG, "requestOpenTONApp")
            transport.requestOpenTONApp()
        }

        if (currentApp.name != "TON") {
            delay(2000)
            try {
                while (!transport.isTONAppOpen()) {
                    delay(1000)
                }
            } catch (e: Throwable) {
                Log.e(LOG_TAG, "TON app not open", e)
            }
        }

        /*if (!transport.isValidVersion()) {
            throw Throwable("Invalid version")
        }*/

        Log.d(LOG_TAG, "ready to use")
        currentTransport = transport
        _stateFlow.value = Ledger.State.ReadyToUse
    }

    override fun onCleared() {
        super.onCleared()
        deviceFlowJob?.cancel()
        currentTransport?.close()
    }

    private companion object {

        const val LOG_TAG = "LedgerConnectLog"

        private val baseUsbTasks = arrayOf(
            Ledger.Task.ConnectUSB,
            Ledger.Task.OpenTONApp
        )

        private val baseBleTasks = arrayOf(
            Ledger.Task.ConnectBLE,
            Ledger.Task.OpenTONApp
        )

        private suspend fun TonTransport.isValidVersion(): Boolean {
            val version = getTonAppVersion()
            return !version.isVersionLowerThan(TonTransport.REQUIRED_VERSION)
        }

    }
}