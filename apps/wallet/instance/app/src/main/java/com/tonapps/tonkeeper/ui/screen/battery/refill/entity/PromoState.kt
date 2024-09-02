package com.tonapps.tonkeeper.ui.screen.battery.refill.entity

sealed class PromoState {
    data object Default : PromoState()
    data object Error : PromoState()
    data class Loading(
        val initialPromo: String? = null
    ) : PromoState()
    data class Applied(
        val appliedPromo: String
    ) : PromoState()
}
