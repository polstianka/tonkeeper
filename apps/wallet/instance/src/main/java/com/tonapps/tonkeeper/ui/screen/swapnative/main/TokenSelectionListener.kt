package com.tonapps.tonkeeper.ui.screen.swapnative.main

interface TokenSelectionListener {

    fun onSellTokenSelected(contractAddress : String)

    fun onBuyTokenSelected(contractAddress : String)

}