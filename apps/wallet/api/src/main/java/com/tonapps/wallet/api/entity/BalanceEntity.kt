package com.tonapps.wallet.api.entity

import android.os.Parcelable
import android.util.Log
import com.tonapps.blockchain.Coin
import io.tonapi.models.JettonBalance
import io.tonapi.models.TokenRates
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.math.BigInteger

@Parcelize
data class BalanceEntity(
    val token: TokenEntity,
    val nano: BigInteger,
    val walletAddress: String,
    val stake: BalanceStakeEntity?,
): Parcelable {

    @IgnoredOnParcel
    var rates: TokenRates? = null

    val value: Float get() = Coin.toCoins(nano, token.decimals)

    val stakeOrTokenValue: Float get() = stake?.value ?: value

    constructor(jettonBalance: JettonBalance) : this(
        token = TokenEntity(jettonBalance.jetton),
        nano = BigInteger(jettonBalance.balance),
        walletAddress = jettonBalance.walletAddress.address,
        stake = null
    ) {
        rates = jettonBalance.price
        Log.d("ConfirmScreenFeatureLog", "jettonBalance = $jettonBalance")
    }
}