package com.tonapps.tonkeeper.ui.screen.swapnative.confirm

import android.os.Bundle
import com.tonapps.wallet.data.token.entities.AssetEntity
import com.tonapps.wallet.data.token.entities.SwapSimulateEntity
import uikit.base.BaseArgs

data class SwapConfirmArgs(
    val fromAsset: AssetEntity?,
    val toAsset: AssetEntity?,
    val swapDetail: SwapSimulateEntity?,
) : BaseArgs() {

    companion object {
        private const val FROM_ASSET = "from_asset"
        private const val TO_ASSET = "to_asset"
        private const val SWAP_DETAIL = "swap_detail"
    }

    constructor(bundle: Bundle) : this(
        fromAsset = bundle.getParcelable(FROM_ASSET),
        toAsset = bundle.getParcelable(TO_ASSET),
        swapDetail = bundle.getParcelable(SWAP_DETAIL),
    )

    override fun toBundle() = Bundle().apply {
        putParcelable(FROM_ASSET, fromAsset)
        putParcelable(TO_ASSET, toAsset)
        putParcelable(SWAP_DETAIL, swapDetail)
    }
}