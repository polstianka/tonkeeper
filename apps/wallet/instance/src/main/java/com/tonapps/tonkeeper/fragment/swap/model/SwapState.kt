package com.tonapps.tonkeeper.fragment.swap.model

import android.net.Uri
import android.os.Parcelable
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.stonfi.entities.StonfiAsset
import com.tonapps.wallet.data.stonfi.entities.StonfiSimulate
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import kotlinx.parcelize.Parcelize

data class SwapState(
    val wallet: WalletLegacy,
    val mapAssets: Map<String, StonfiAsset>,
    val mapTokens: Map<String, AccountTokenEntity>,
    val pairs: Map<String, List<String>>,
    val send: TokenState,
    val receive: TokenState?,
    val simulate: StonfiSimulate?,
    val offerAmount: String? = null,
    val minAskAmount: String? = null
) {
    @Parcelize
    data class TokenState(
        val balance: CharSequence,
        val imageUri: Uri?,
        val symbol: CharSequence,
        val value: Float,
        val address: String,
    ): Parcelable {
        val isTon = symbol == "TON"
    }

    val sendToken
        get() = send.let { mapTokens[it.symbol] }
    val sendAsset
        get() = send.let { mapAssets[it.symbol] }!!
    val receiveToken
        get() = receive?.let { mapTokens[it.symbol] }
    val receiveAsset
        get() = receive?.let { mapAssets[it.symbol] }

}