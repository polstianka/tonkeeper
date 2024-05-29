package com.tonapps.tonkeeper.fragment.swap.pick_asset

import android.os.Bundle
import com.tonapps.extensions.getEnum
import com.tonapps.extensions.putEnum
import com.tonapps.wallet.api.entity.TokenEntity
import uikit.base.BaseArgs

data class PickAssetArgs(
    val type: PickAssetType,
    val toSend: TokenEntity?,
    val toReceive: TokenEntity?
) : BaseArgs() {

    companion object {
        private const val KEY_TYPE = "KEY_TYPE "
        private const val KEY_TO_SEND = "KEY_TO_SEND "
        private const val KEY_TO_RECEIVE = "KEY_TO_RECEIVE"

    }

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putEnum(KEY_TYPE, type)
            putParcelable(KEY_TO_RECEIVE, toReceive)
            putParcelable(KEY_TO_SEND, toSend)
        }
    }

    constructor(bundle: Bundle) : this(
        type = bundle.getEnum(KEY_TYPE, PickAssetType.SEND),
        toSend = bundle.getParcelable(KEY_TO_SEND),
        toReceive = bundle.getParcelable(KEY_TO_RECEIVE)
    )
}
