package com.tonapps.tonkeeper.ui.screen.swap

import com.tonapps.tonkeeper.api.swap.StonfiSwapAsset
import com.tonapps.tonkeeper.api.swap.SwapSimulateData
import com.tonapps.wallet.data.token.entities.AccountTokenEntity

data class SwapData(
    val addressFrom: String? = null,
    val addressTo: String? = null,
    val name: String? = null,
    val comment: String? = null,
    val max: Boolean = false,
    val token: AccountTokenEntity? = null,
    val bounce: Boolean = false,
    var currentFrom: Boolean = false,
    val from: StonfiSwapAsset? = null,
    val to: StonfiSwapAsset? = null,
    val amountRaw: String = "0",
    val amountToRaw: String = "0",
    val simulateData: SwapSimulateData? = null,
    val slippage: Float = 0.001f,
    val expertMode: Boolean = false,
    var flip: Boolean = false,
    var initialTo: String? = null
)