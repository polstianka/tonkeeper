package com.tonapps.tonkeeper.ui.screen.swap.main

import android.net.Uri
import android.os.Bundle
import com.tonapps.extensions.getParcelableCompat
import com.tonapps.extensions.getSerializableCompat
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapRequest
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapTransfer
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletType
import uikit.base.BaseArgs

data class SwapArgs (
    val localId: String,
    val uri: Uri,
    val address: String,
    val walletType: WalletType,
    val sendToken: TokenEntity? = null,
    val receiveToken: TokenEntity? = null,
    val confirmation: SwapRequest? = null
): BaseArgs() {
    private companion object {
        private const val ARG_LOCAL_ID = "local_id"
        private const val ARG_URI = "uri"
        private const val ARG_ADDRESS = "address"
        private const val ARG_WALLET_TYPE = "type"
        private const val ARG_SEND_TOKEN = "send_token"
        private const val ARG_RECEIVE_TOKEN = "receive_token"
        private const val ARG_CONFIRMATION = "confirm"
    }

    init {
        if (confirmation != null && (sendToken == null || receiveToken == null)) {
            error("Missing token swap information")
        }
    }

    constructor(bundle: Bundle) : this(
        localId = bundle.getString(ARG_LOCAL_ID)!!,
        uri = bundle.getParcelableCompat(ARG_URI)!!,
        address = bundle.getString(ARG_ADDRESS)!!,
        walletType = bundle.getSerializableCompat(ARG_WALLET_TYPE)!!,
        sendToken = bundle.getParcelableCompat(ARG_SEND_TOKEN),
        receiveToken = bundle.getParcelableCompat(ARG_RECEIVE_TOKEN),
        confirmation = bundle.getParcelableCompat(ARG_CONFIRMATION)
    )

    override fun toBundle(): Bundle = Bundle().apply {
        putString(ARG_LOCAL_ID, localId)
        putParcelable(ARG_URI, uri)
        putString(ARG_ADDRESS, address)
        putSerializable(ARG_WALLET_TYPE, walletType)
        sendToken?.let {
            putParcelable(ARG_SEND_TOKEN, it)
        }
        receiveToken?.let {
            putParcelable(ARG_RECEIVE_TOKEN, it)
        }
        confirmation?.let {
            putParcelable(ARG_CONFIRMATION, it)
        }
    }

    val isConfirmation: Boolean
        get() = confirmation != null
}