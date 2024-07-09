package com.tonapps.tonkeeper.fragment.swap.pick_asset

import android.os.Bundle
import com.tonapps.extensions.getEnum
import com.tonapps.extensions.putEnum
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetBalance
import uikit.base.BaseArgs

data class PickAssetResult(
    val asset: DexAssetBalance,
    val type: PickAssetType
) : BaseArgs() {
    companion object {
        private const val KEY_ASSET = "KEY_ASSET "
        private const val KEY_TYPE = "KEY_TYPE "
        const val REQUEST_KEY = "PickAssetResult"
    }
    override fun toBundle(): Bundle {
        return Bundle().apply {
            putParcelable(KEY_ASSET, asset)
            putEnum(KEY_TYPE, type)
        }
    }

    constructor(bundle: Bundle): this(
        asset = bundle.getParcelable(KEY_ASSET)!!,
        type = bundle.getEnum(KEY_TYPE, PickAssetType.SEND)
    )
}
