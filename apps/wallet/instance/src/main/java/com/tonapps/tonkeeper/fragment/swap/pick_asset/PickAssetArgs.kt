package com.tonapps.tonkeeper.fragment.swap.pick_asset

import android.os.Bundle
import com.tonapps.extensions.getEnum
import com.tonapps.extensions.putEnum
import uikit.base.BaseArgs

data class PickAssetArgs(
    val type: PickAssetType
) : BaseArgs() {

    companion object {
        private const val KEY_TYPE = "KEY_TYPE "
    }

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putEnum(KEY_TYPE, type)
        }
    }

    constructor(bundle: Bundle) : this(
        type = bundle.getEnum(KEY_TYPE, PickAssetType.SEND)
    )
}
