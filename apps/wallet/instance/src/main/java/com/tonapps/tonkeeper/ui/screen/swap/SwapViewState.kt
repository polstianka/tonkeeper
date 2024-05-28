package com.tonapps.tonkeeper.ui.screen.swap

data class SwapViewState(
    var fromTokenTitle: String?,
    var toTokenTitle: String?,
    var fromTokenIcon: String?,
    var toTokenIcon: String?,
    var amount: String,
    var balance: String,
    var swapButton: String
)