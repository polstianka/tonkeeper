package com.tonapps.wallet.data.rates

import android.content.Context
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.entity.RateEntity
import com.tonapps.wallet.data.rates.entity.RatesEntity
import com.tonapps.wallet.data.rates.source.BlobDataSource
import io.tonapi.models.TokenRates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RatesRepository(
    context: Context,
    private val api: API
) {

    private val localDataSource = BlobDataSource(context)

    fun cache(currency: WalletCurrency, tokens: List<String>): RatesEntity {
        return localDataSource.get(currency).filter(tokens)
    }

    suspend fun load(currency: WalletCurrency, token: String) {
        load(currency, mutableListOf(token))
    }

    suspend fun load(
        currency: WalletCurrency,
        tokens: MutableList<String>
    ) = withContext(Dispatchers.IO) {
        if (!tokens.contains("TON")) {
            tokens.add("TON")
        }
        val rates = api.getRates(currency.code, tokens)
        insertRates(currency, rates)
    }

    fun insertRates(currency: WalletCurrency, rates: Map<String, TokenRates>) {
        if (rates.isEmpty()) {
            return
        }
        val entities = mutableListOf<RateEntity>()
        for (rate in rates) {
            entities.add(RateEntity(currency, rate.key, rate.value))
        }
        localDataSource.add(currency, entities)
    }

    fun getRates(currency: WalletCurrency, token: String): RatesEntity {
        return getRates(currency, listOf(token))
    }

    fun getRates(currency: WalletCurrency, tokens: List<String>): RatesEntity {
        return localDataSource.get(currency).filter(tokens)
    }
}