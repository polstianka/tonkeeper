package com.tonapps.wallet.api.entity

import android.os.Parcelable
import android.util.Log
import com.tonapps.blockchain.Coin
import io.tonapi.models.JettonBalance
import io.tonapi.models.TokenRates
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class BalanceEntity(
    val token: TokenEntity,
    @Deprecated("""
        32-bit float isn't suitable for coins, and causes a lot bugs in different parts of the app.
        
        Rework to java.math.BigDecimal for coins and org.ton.bigint.BigInt for nano units.
        
        See deprecations in com.tonapps.blockchain.Coin
    """) val value: Float,
    val walletAddress: String
): Parcelable {

    @IgnoredOnParcel
    var rates: TokenRates? = null

    constructor(jettonBalance: JettonBalance) : this(
        token = TokenEntity(jettonBalance.jetton),
        value = Coin.parseJettonBalance(jettonBalance.balance, jettonBalance.jetton.decimals),
        walletAddress = jettonBalance.walletAddress.address,
    ) {
        rates = jettonBalance.price
        Log.d("ConfirmScreenFeatureLog", "jettonBalance = $jettonBalance")
    }
}