package com.tonapps.wallet.api.entity

import android.os.Parcelable
import com.tonapps.icu.Coins
import io.tonapi.models.JettonBalance
import io.tonapi.models.TokenRates
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class BalanceEntity(
    val token: TokenEntity,
    val value: Coins,
    val walletAddress: String,
    val initializedAccount: Boolean
): Parcelable {

    companion object {

        fun empty(accountId: String) = BalanceEntity(
            token = TokenEntity.TON,
            value = Coins.ZERO,
            walletAddress = accountId,
            initializedAccount = false,
        )
    }

    @IgnoredOnParcel
    var rates: TokenRates? = null

    val isTon: Boolean
        get() = token.isTon

    constructor(jettonBalance: JettonBalance) : this(
        token = TokenEntity(jettonBalance.jetton),
        value = Coins.of(BigDecimal(jettonBalance.balance).movePointLeft(jettonBalance.jetton.decimals), jettonBalance.jetton.decimals),
        walletAddress = jettonBalance.walletAddress.address,
        initializedAccount = true,
    ) {
        rates = jettonBalance.price
    }
}