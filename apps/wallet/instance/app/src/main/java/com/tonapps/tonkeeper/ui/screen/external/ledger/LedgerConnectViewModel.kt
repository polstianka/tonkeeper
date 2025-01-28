package com.tonapps.tonkeeper.ui.screen.external.ledger

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.ledger.live.ble.model.BleDeviceModel
import com.tonapps.extensions.bestMessage
import com.tonapps.ledger.ble.LedgerBle
import com.tonapps.ledger.ton.TonTransport
import com.tonapps.ledger.usb.LedgerUsb
import com.tonapps.tonkeeper.extensions.isVersionLowerThan
import com.tonapps.tonkeeper.ui.base.BaseWalletVM
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
        currentTransport = null
        _stateFlow.value = Ledger.State.WaitingForConnection
        ledgerBle.deviceListener { device ->
            setBleDevice(device)
        }
    }

    private fun startUSBDeviceListener() {
        currentTransport = null
        _stateFlow.value = Ledger.State.WaitingForConnection
        deviceFlowJob = ledgerUsb.deviceFlow()
            .map(ledgerUsb::connectDevice)
            .map(ledgerUsb::createTonTransport)
            .map(::setTonTransport)
            .catch {
                toast(it.bestMessage)
                _stateFlow.value = Ledger.State.Idle
            }.launchIn(viewModelScope)
    }

    private fun setBleDevice(device: BleDeviceModel) {
        viewModelScope.launch {
            try {
                val connected = ledgerBle.connectDevice(device)
                val tonTransport = ledgerBle.createTonTransfer(connected)
                setTonTransport(tonTransport)
            } catch (e: Throwable) {
                toast(e.bestMessage)
                _stateFlow.value = Ledger.State.Idle
            }
        }
    }

    private suspend fun setTonTransport(transport: TonTransport) {
        _stateFlow.value = Ledger.State.WaitingAppTON
        val currentApp = transport.getCurrentApp()
        if (currentApp.name == "BOLOS") {
            transport.requestOpenTONApp()
        }

        if (currentApp.name != "TON") {
            delay(2000)
            while (!transport.isTONAppOpen()) {
                delay(1000)
            }
        }

        /*if (!transport.isValidVersion()) {
            throw Throwable("Invalid version")
        }*/
        currentTransport = transport
        _stateFlow.value = Ledger.State.ReadyToUse
    }

    override fun onCleared() {
        super.onCleared()
        deviceFlowJob?.cancel()
        currentTransport?.close()
    }

    private companion object {

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