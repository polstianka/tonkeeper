package com.tonapps.tonkeeper.api.rates

import android.content.Context
import com.tonapps.wallet.api.Tonapi
import com.tonapps.tonkeeper.api.base.BaseBlobRepository
import com.tonapps.wallet.api.core.SourceAPI
import com.tonapps.tonkeeper.api.fromJSON
import com.tonapps.tonkeeper.api.toJSON
import com.tonapps.tonkeeper.api.withRetry
import com.tonapps.wallet.data.core.WalletCurrency
import io.tonapi.apis.RatesApi
import io.tonapi.models.GetRates200Response
import io.tonapi.models.TokenRates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RatesRepository(
    private val context: Context = com.tonapps.tonkeeper.App.instance,
    private val api: SourceAPI<RatesApi> = Tonapi.rates
): BaseBlobRepository<GetRates200Response>("rates", context) {

    suspend fun sync(
        accountId: String,
        testnet: Boolean,
        jettons: List<String>
    ): GetRates200Response {

        val response = fromCloud(
            tokens = WalletCurrency.CRYPTO + jettons,
            currency = WalletCurrency.FIAT,
            testnet = testnet
        )

        setMemory(accountId, testnet, response)

        saveCache(
            accountId = accountId,
            testnet = testnet,
            blob = toJSON(response)
        )

        return response
    }

    suspend fun get(accountId: String, testnet: Boolean): GetRates200Response? {
        val cache = fromCache(accountId, testnet)
        if (cache != null) {
            return cache
        }
        return sync(accountId, testnet, emptyList())
    }

    private suspend fun fromCloud(
        tokens: List<String>,
        currency: List<String>,
        testnet: Boolean,
    ): GetRates200Response {
        val rates = mutableMapOf<String, TokenRates>()
        val chunks = tokens.chunked(100)
        for (chunk in chunks) {
            val response = fromCloud(
                tokens = chunk,
                currency = currency,
                testnet = testnet
            )?.rates ?: continue
            rates.putAll(response)
        }
        return GetRates200Response(rates)
    }

    override fun onParse(
        blob: String
    ): GetRates200Response {
        return fromJSON(blob)
    }

}