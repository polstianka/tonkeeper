package com.tonapps.tonkeeper.ui.screen.buysell.main

data class BuySellScreenState(
    val currencyCode: String,
    val continueState: ContinueState = ContinueState.ENTER_AMOUNT,
    val insufficientBalance: Boolean = false,
    val rate: CharSequence = "0",
    val amount: Double = 0.0,
    val available: CharSequence = "",
) {

    sealed class ContinueState {
        object NEXT : ContinueState()
        object SELECT_PAYMENT : ContinueState()
        object ENTER_AMOUNT : ContinueState()
        object DISABLE : ContinueState()
    }

    companion object {
        val initState = BuySellScreenState(currencyCode = "")
    }
}