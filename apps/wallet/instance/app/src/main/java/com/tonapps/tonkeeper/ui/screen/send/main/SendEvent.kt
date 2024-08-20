package com.tonapps.tonkeeper.ui.screen.send.main

import com.tonapps.icu.Coins

sealed class SendEvent {
    data object Failed: SendEvent()
    data object Success: SendEvent()
    data object Loading: SendEvent()
    data object InsufficientBalance: SendEvent()
    data object Confirm: SendEvent()
    data class Fee(
        val value: Coins,
        val format: CharSequence,
        val convertedFormat: CharSequence,
        val isBattery: Boolean,
    ): SendEvent()
}