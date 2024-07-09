package com.tonapps.tonkeeper.fragment.swap.domain.model

import android.os.Bundle

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

private const val KEY_EXPERT = "KEY_EXPERT "
private const val KEY_SLIPPAGE = "KEY_SLIPPAGE "
fun SwapSettings.toBundle(): Bundle {
    return Bundle().apply {
        putBoolean(KEY_EXPERT, isExpertModeOn)
        putInt(KEY_SLIPPAGE, slippagePercent)
    }
}

fun Bundle.toSwapSettings(): SwapSettings {
    val isExpert = getBoolean(KEY_EXPERT, false)
    val slippage = getInt(KEY_SLIPPAGE, 1)
    return when (isExpert) {
        true -> SwapSettings.ExpertMode(slippage)
        false -> when (slippage) {
            1 -> SwapSettings.NoviceMode.One
            3 -> SwapSettings.NoviceMode.Three
            5 -> SwapSettings.NoviceMode.Five
            else -> SwapSettings.NoviceMode.One
        }
    }
}