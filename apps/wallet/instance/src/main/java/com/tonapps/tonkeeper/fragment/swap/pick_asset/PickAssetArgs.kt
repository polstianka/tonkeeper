package com.tonapps.tonkeeper.fragment.swap.pick_asset

import android.os.Bundle
import com.tonapps.extensions.getEnum
import com.tonapps.extensions.putEnum
import com.tonapps.wallet.api.entity.TokenEntity
import uikit.base.BaseArgs

data class PickAssetArgs(
    val type: PickAssetType,
    val pickedItems: List<TokenEntity>
) : BaseArgs() {

    companion object {
        private const val KEY_TYPE = "KEY_TYPE "
        private const val KEY_PICKED_ITEMS = "KEY_PICKED_ITEMS "
    }

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putEnum(KEY_TYPE, type)
            putParcelableArray(KEY_PICKED_ITEMS, pickedItems.toTypedArray())
        }
    }

    constructor(bundle: Bundle) : this(
        type = bundle.getEnum(KEY_TYPE, PickAssetType.SEND),
        pickedItems = bundle.getParcelableArray(KEY_PICKED_ITEMS)!!
            .filterIsInstance<TokenEntity>()
    )
}
