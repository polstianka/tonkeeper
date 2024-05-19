package com.tonapps.wallet.data.rates.entity

import android.os.Parcelable
import com.tonapps.wallet.data.core.WalletCurrency
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class RatesEntity(
    val currency: WalletCurrency,
    private val map: HashMap<String, RateEntity>
): Parcelable {

    companion object {
        fun empty(currency: WalletCurrency): RatesEntity {
            return RatesEntity(currency, hashMapOf())
        }
    }

    val isEmpty: Boolean
        get() = map.isEmpty()

    fun merge(rates: RatesEntity) {
        for ((key, value) in rates.map) {
            map[key] = value
        }
    }

    fun merge(rates: List<RateEntity>): RatesEntity {
        for (rate in rates) {
            map[rate.tokenCode] = rate
        }
        return this
    }

    fun filter(tokens: List<String>): RatesEntity {
        val result = hashMapOf<String, RateEntity>()
        for (token in tokens) {
            val rate = map[token] ?: continue
            result[token] = rate
        }
        return RatesEntity(currency, result)
    }

    fun rate(token: String): RateEntity? {
        return map[token]
    }

    fun rateValue(token: String): BigDecimal {
        return rate(token)?.value ?: BigDecimal.ZERO
    }

    fun rateDiff(token: String): RateDiffEntity? {
        return rate(token)?.diff
    }

    fun convert(token: String, value: BigDecimal): BigDecimal {
        return value * rateValue(token)
    }

    fun getRate(token: String): BigDecimal {
        return rateValue(token)
    }

    fun getDiff24h(token: String): String {
        return rateDiff(token)?.diff24h ?: ""
    }

    fun getDiff7d(token: String): String {
        return rateDiff(token)?.diff7d ?: ""
    }

    fun getDiff30d(token: String): String {
        return rateDiff(token)?.diff30d ?: ""
    }
}