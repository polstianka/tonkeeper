package com.tonapps.tonkeeper.fragment.trade.root.vm

sealed class BuySellEvent {
    object NavigateToPickCountry : BuySellEvent()
}