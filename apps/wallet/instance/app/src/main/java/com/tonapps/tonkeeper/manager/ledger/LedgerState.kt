package com.tonapps.tonkeeper.manager.ledger

import com.ledger.live.ble.model.BleDeviceModel
import com.tonapps.tonkeeper.manager.ledger.device.LedgerDevice
import com.tonapps.tonkeeper.ui.screen.ledger.steps.ConnectedDevice

sealed class LedgerState {
    data object Scanning: LedgerState()
    data object Found: LedgerState()
    data object Connected: LedgerState()
    data object TonAppOpened: LedgerState()
}

/*
sealed class ConnectionState {
    data object Idle: ConnectionState()
    data object Scanning: ConnectionState()
    data object Connected: ConnectionState()
    data object TonAppOpened: ConnectionState()
    data object Signed: ConnectionState()
    data class Disconnected(val error: BleError? = null): ConnectionState()
}
 */