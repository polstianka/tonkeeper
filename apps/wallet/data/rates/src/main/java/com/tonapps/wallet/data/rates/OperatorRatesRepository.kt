package com.tonapps.wallet.data.rates

import android.content.Context
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.entity.OperatorBuyRateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OperatorRatesRepository(
    context: Context,
    private val api: API
) {

    private val cache: MutableMap<String, List<OperatorBuyRateEntity>> = mutableMapOf()

    suspend fun getRates(currency: WalletCurrency): List<OperatorBuyRateEntity> {
        val rates = getCache(currency)

        return rates.ifEmpty { getRemote(currency) }
    }

    fun getCache(currency: WalletCurrency): List<OperatorBuyRateEntity> {
        return cache[currency.code] ?: emptyList()
    }

    suspend fun getRemote(currency: WalletCurrency): List<OperatorBuyRateEntity> =
        withContext(Dispatchers.IO) {
            val response = api.getOperatorRates(currency.code).map {
                OperatorBuyRateEntity(it)
            }

            if (response.isNotEmpty())
                cache.put(currency.code, response)

            response
        }


}