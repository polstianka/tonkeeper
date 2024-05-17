package com.tonapps.tonkeeper.fragment.swap.domain.model

sealed class SwapSettings(
    val isExpertModeOn: Boolean,
    val slippagePercent: Int
) {
    class ExpertMode(percent: Int) : SwapSettings(true, percent)

    sealed class NoviceMode(slippagePercent: Int) : SwapSettings(
        isExpertModeOn = false, slippagePercent = slippagePercent
    ) {
        object One : NoviceMode(1)
        object Three : NoviceMode(3)
        object Five : NoviceMode(5)
    }
}