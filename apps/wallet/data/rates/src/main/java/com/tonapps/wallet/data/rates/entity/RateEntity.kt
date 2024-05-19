package com.tonapps.wallet.data.rates.entity

import android.os.Parcelable
import com.tonapps.wallet.data.core.WalletCurrency
import io.tonapi.models.TokenRates
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class RateEntity(
    val tokenCode: String,
    val currency: WalletCurrency,
    val value: BigDecimal,
    val diff: RateDiffEntity
): Parcelable {

    constructor(
        currency: WalletCurrency,
        token: String,
        rates: TokenRates
    ) : this(
        tokenCode = token,
        currency = currency,
        value = rates.prices?.get(currency.code) ?: BigDecimal.ZERO,
        diff = RateDiffEntity(currency, rates)
    )
}