package com.tonapps.tonkeeper.extensions

import com.tonapps.icu.CurrencyFormatter
import com.tonapps.wallet.data.rates.entity.RatesEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import java.math.BigDecimal

fun formattedRate(
    rateFlow: Flow<RatesEntity>,
    amountFlow: Flow<BigDecimal>,
    token: String,
    formatter: (currencyCode: String, amount: BigDecimal) -> CharSequence = CurrencyFormatter::format
): Flow<CharSequence> = combine(rateFlow, amountFlow) { rates, amount ->
    formatRate(rates, amount, token, formatter)
}.filterNotNull()

fun formatRate(
    rates: RatesEntity,
    amount: BigDecimal,
    token: String,
    formatter: (currencyCode: String, amount: BigDecimal) -> CharSequence = CurrencyFormatter::format
): CharSequence? {
    val rate = rates.rate(token) ?: return null
    val totalAmount = rate.value * amount
    return formatter(rates.currency.code, totalAmount)
}