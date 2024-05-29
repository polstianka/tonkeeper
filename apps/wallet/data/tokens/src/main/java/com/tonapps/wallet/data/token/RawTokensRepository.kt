package com.tonapps.wallet.data.token

import android.content.Context
import com.tonapps.extensions.toByteArray
import com.tonapps.extensions.toParcel
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.BalanceEntity
import com.tonapps.wallet.data.core.BlobDataSource
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.core.api.ApiDataRepository
import com.tonapps.wallet.data.core.api.Repository
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.token.entities.BalanceListEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class RawTokensRepository(
    ratesRepository: RatesRepository,
    context: Context,
    api: API,
) {
    private val source = RemoteDataSource(context, ratesRepository, api)
    val storageFlow = source.storageFlow

    suspend fun doRequest(req: Request) = source.doRequest(req)

    suspend fun doRequest(accountId: String,  testnet: Boolean, currency: WalletCurrency) =
        doRequest(Request(accountId = accountId, testnet = testnet, currency = currency))


    @Deprecated("Use flows")
    suspend fun getRemote(
        currency: WalletCurrency,
        accountId: String,
        testnet: Boolean,
    ): List<BalanceEntity> = withContext(Dispatchers.IO) {
        source.doRequest(Request(accountId = accountId, testnet = testnet, currency = currency))!!.list
    }

    @Deprecated("Use flows")
    fun getLocal(
        accountId: String,
        testnet: Boolean,
    ): List<BalanceEntity>? {
        return source.get(Request(accountId = accountId, testnet = testnet, currency = WalletCurrency.DEFAULT))?.result?.list
    }

    /* * */

    data class Request(
        val accountId: String,
        val testnet: Boolean,
        val currency: WalletCurrency
    ) : com.tonapps.wallet.data.core.api.Request {
        override fun cacheKey(): String {
            val key = "wallet_${accountId}"

            if (!testnet) {
                return key
            }
            return "${key}_testnet"
        }

        override fun requestKey(): String = Companion.requestKey(accountId, testnet, currency)

        companion object {
            fun requestKey(accountId: String, testnet: Boolean, currency: WalletCurrency): String {
                val key = "wallet_${currency.code}_${accountId}"

                if (!testnet) {
                    return key
                }
                return "${key}_testnet"
            }
        }
    }

    data class Storage(
        private val source: RemoteDataSource
    ): Repository<Request, BalanceListEntity> {
        fun get( accountId: String, testnet: Boolean, currency: WalletCurrency) = get(Request(accountId = accountId, testnet = testnet, currency = currency))

        override fun get(req: Request): ApiDataRepository.Result<BalanceListEntity>? = source.get(req)
    }

    class RemoteDataSource(
        context: Context,
        private val ratesRepository: RatesRepository,
        private val api: API,
    ) : ApiDataRepository<Request, BalanceListEntity, Storage>() {
        private val localDataSource = LocalDataSource(context)

        override fun storage(): Storage = Storage(this)

        override suspend fun requestImpl(request: Request): BalanceListEntity = withContext(Dispatchers.IO) {
            val tonRateDefferred = if (!request.testnet) async {
                ratesRepository.load(request.currency, "TON", allowInsert = false)
            } else null

            val tonBalanceDeferred = async {
                api.getTonBalance(request.accountId, request.testnet)
            }

            val jettonBalancesDeferred = async {
                api.getJettonsBalances(request.accountId, request.testnet, request.currency.code)
            }

            val account = tonBalanceDeferred.await()
            val jettons = jettonBalancesDeferred.await()

            if (!request.testnet) {
                val rate = tonRateDefferred!!.await()
                val rates = ratesRepository.toRatesMap(jettons)
                for (r in rate) {
                    rates[r.key] = r.value
                }

                ratesRepository.insertRates(request.currency, rates)
            }
            BalanceListEntity(list = listOf(account) + jettons)
        }

        override fun getCache(key: String): BalanceListEntity? {
            return localDataSource.getCache(key)
        }

        override fun clearCache(key: String) {
            localDataSource.clearCache(key)
        }

        override fun setCache(key: String, value: BalanceListEntity): Boolean {
            return localDataSource.updateCache(key, value)
        }
    }

    private class LocalDataSource(context: Context): BlobDataSource<BalanceListEntity>(
        context = context,
        path = "wallet",
        lruInitialCapacity = 12
    ) {
        override fun onMarshall(data: BalanceListEntity) = data.toByteArray()

        override fun onUnmarshall(bytes: ByteArray) = bytes.toParcel<BalanceListEntity>()
    }
}