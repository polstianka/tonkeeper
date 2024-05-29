package com.tonapps.wallet.data.rates

import android.content.Context
import androidx.collection.ArrayMap
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.BalanceEntity
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.core.api.ApiDataRepository
import com.tonapps.wallet.data.core.api.Repository
import com.tonapps.wallet.data.rates.entity.RateEntity
import com.tonapps.wallet.data.rates.entity.RatesEntity
import com.tonapps.wallet.data.rates.source.BlobDataSource
import io.tonapi.models.TokenRates

class RatesRepository(
    context: Context,
    private val api: API
) {

    private val localDataSource = BlobDataSource(context)

    fun cache(currency: WalletCurrency, tokens: List<String>): RatesEntity {
        return localDataSource.get(currency).filter(tokens)
    }

    fun load(currency: WalletCurrency, token: String, allowInsert: Boolean = true): Map<String, TokenRates> {
        return load(currency, mutableListOf(token), allowInsert)
    }

    fun load(currency: WalletCurrency, tokens: MutableList<String>, allowInsert: Boolean = true): Map<String, TokenRates> {
        if (!tokens.contains("TON")) {
            tokens.add("TON")
        }
        val rates = api.getRates(currency.code, tokens)
        if (allowInsert) {
            insertRates(currency, rates)
        }
        return rates
    }

    fun insertRates(currency: WalletCurrency, rates: Map<String, TokenRates>) {
        if (rates.isEmpty()) {
            return
        }
        val entities = mutableListOf<RateEntity>()
        for (rate in rates) {
            entities.add(RateEntity(currency, rate.key, rate.value))
        }
        if (localDataSource.add(currency, entities)) {
            source.emit(currency.code)
        }
    }

    fun getRates(currency: WalletCurrency, token: String): RatesEntity {
        return getRates(currency, listOf(token))
    }

    fun getRates(currency: WalletCurrency, tokens: List<String>): RatesEntity {
        return localDataSource.get(currency).filter(tokens)
    }

    fun toRatesMap(list: List<BalanceEntity>): MutableMap<String, TokenRates> {
        val rates = ArrayMap<String, TokenRates>()
        for (balance in list) {
            balance.rates?.let {
                rates[balance.token.address] = it
            }
        }
        return rates
    }




    /* * */

    private val source = RemoteDataSource(localDataSource, api)
    val storageFlow = source.storageFlow

    fun storage() = source.storage()

    suspend fun doRequest(currency: WalletCurrency, tokens: MutableList<String>) {
        source.doRequest(Request(currency, tokens))
    }

    data class Request(
        val currency: WalletCurrency,
        val tokens: List<String>
    ) : com.tonapps.wallet.data.core.api.Request {

        constructor(currency: WalletCurrency): this(currency, emptyList())

        override fun cacheKey(): String = currency.code

        override fun requestKey(): String {
            val b = StringBuilder(currency.code)
            for (token in tokens) {
                b.append('_')
                b.append(token)
            }

            if (!tokens.contains("TON")) {
                b.append("_TON")
            }

            return b.toString()
        }
    }

    data class Storage(
        private val source: RemoteDataSource
    ): Repository<Request, RatesEntity> {
        fun get(currency: WalletCurrency) = get(Request(currency))

        override fun get(req: Request): ApiDataRepository.Result<RatesEntity>? = source.get(req)
    }

    class RemoteDataSource(
        private val localDataSource: BlobDataSource,
        private val api: API,
    ) : ApiDataRepository<Request, RatesEntity, Storage>() {
        override suspend fun requestImpl(request: Request): RatesEntity {
            val tokens = request.tokens.toMutableList()
            if (!tokens.contains("TON")) {
                tokens.add("TON")
            }

            val rates = api.getRates(request.currency.code, tokens)
            val entities = mutableListOf<RateEntity>()
            for (rate in rates) {
                entities.add(RateEntity(request.currency, rate.key, rate.value))
            }

            val res = RatesEntity(request.currency, HashMap())
            res.merge(entities)
            return res
        }

        public override fun storage(): Storage = Storage(this)

        public override fun getCache(key: String): RatesEntity? {
            return localDataSource.getCache(key)
        }

        public override fun clearCache(key: String) {
            localDataSource.clearCache(key)
        }

        public override fun setCache(key: String, value: RatesEntity): Boolean {
            val x = getCache(key)
            x?.merge(value)

            return localDataSource.updateCache(key, x ?: value)
        }
    }
}