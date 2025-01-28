package com.tonapps.tonkeeper.ui.screen.external.ledger

import androidx.annotation.StringRes
import com.tonapps.wallet.localization.Localization

object Ledger {

    enum class ConnectionType {
        NONE, USB, BLE
    }

    enum class State {
        Default, WaitingForConnection, WaitingAppTON, Idle, ReadyToUse
    }

    sealed class Task(@StringRes val label: Int) {
        data object ConnectUSB: Task(Localization.ledger_usb_connect)
        data object OpenTONApp: Task(Localization.ledger_open_ton_app)
        data object ConnectBLE: Task(Localization.ledger_connect)
    }

}