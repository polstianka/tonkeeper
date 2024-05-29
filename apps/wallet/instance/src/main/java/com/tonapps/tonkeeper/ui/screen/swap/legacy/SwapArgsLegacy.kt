package com.tonapps.tonkeeper.ui.screen.swap.legacy

import android.net.Uri
import android.os.Bundle
import com.tonapps.extensions.getParcelableCompat
import uikit.base.BaseArgs

@Deprecated(message = "Migrate to the new SwapScreen")
data class SwapArgsLegacy(
    val uri: Uri,
    val address: String,
    val fromToken: String,
    val toToken: String?,
): BaseArgs() {

    private companion object {
        private const val ARG_URI = "uri"
        private const val ARG_ADDRESS = "address"
        private const val FROM_TOKEN = "from"
        private const val TO_TOKEN = "to"
    }

    constructor(bundle: Bundle) : this(
        uri = bundle.getParcelableCompat(ARG_URI)!!,
        address = bundle.getString(ARG_ADDRESS)!!,
        fromToken = bundle.getString(FROM_TOKEN)!!,
        toToken = bundle.getString(TO_TOKEN)
    )

    override fun toBundle(): Bundle = Bundle().apply {
        putParcelable(ARG_URI, uri)
        putString(ARG_ADDRESS, address)
        putString(FROM_TOKEN, fromToken)
        toToken?.let {
            putString(TO_TOKEN, it)
        }
    }
}