package com.tonapps.tonkeeper.ui.screen.swap.assets

import android.os.Bundle
import com.tonapps.extensions.getParcelableCompat
import com.tonapps.tonkeeper.ui.screen.swap.data.RemoteAssets
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapTarget
import com.tonapps.wallet.api.entity.TokenEntity
import uikit.base.BaseArgs

data class AssetPickerArgs (
    val requestKey: String,
    val remoteAssets: RemoteAssets,
    val target: SwapTarget,
    val selectedToken: TokenEntity?,
    val limitToMarkets: Set<String>?
): BaseArgs() {

    private companion object {
        private const val ARG_REQUEST_KEY = "key"
        private const val ARG_LIST = "list"
        private const val ARG_IS_SEND = "is_send"
        private const val ARG_SELECTED_TOKEN = "selected"
        private const val ARG_LIMIT_TO_MARKETS = "limit_to_markets"
    }

    constructor(bundle: Bundle) : this(
        requestKey = bundle.getString(ARG_REQUEST_KEY)!!,
        remoteAssets = bundle.getParcelableCompat(ARG_LIST)!!,
        target = if (bundle.getBoolean(ARG_IS_SEND, false)) SwapTarget.SEND else SwapTarget.RECEIVE,
        selectedToken = bundle.getParcelableCompat(ARG_SELECTED_TOKEN),
        limitToMarkets = bundle.let {
            val array = it.getStringArray(ARG_LIMIT_TO_MARKETS)
            array?.toSet()
        }
    )

    override fun toBundle(): Bundle = Bundle().apply {
        putString(ARG_REQUEST_KEY, requestKey)
        putParcelable(ARG_LIST, remoteAssets)
        putBoolean(ARG_IS_SEND, target == SwapTarget.SEND)
        selectedToken?.let {
            putParcelable(ARG_SELECTED_TOKEN, it)
        }
        limitToMarkets?.let {
            putStringArray(ARG_LIMIT_TO_MARKETS, limitToMarkets.toTypedArray())
        }
    }
}