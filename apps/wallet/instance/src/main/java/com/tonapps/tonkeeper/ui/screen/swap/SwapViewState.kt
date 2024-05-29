package com.tonapps.tonkeeper.ui.screen.swap

data class SwapViewState(
    var fromTokenTitle: String?,
    var toTokenTitle: String?,
    var fromTokenIcon: String?,
    var toTokenIcon: String?,
    var toAmount: String,
    var fromAmount: String,
    var swapButton: String,
    var balance: String?
)