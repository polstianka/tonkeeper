package com.tonapps.tonkeeper.ui.screen.card.entity

sealed class CardBridgeEvent {
    data object CloseApp : CardBridgeEvent()
    data class OpenUrl(val url: String) : CardBridgeEvent()
}
