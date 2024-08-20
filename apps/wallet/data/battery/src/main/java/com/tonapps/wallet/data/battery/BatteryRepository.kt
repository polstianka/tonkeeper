package com.tonapps.wallet.data.battery

import android.content.Context
import android.util.ArrayMap
import com.tonapps.blockchain.ton.extensions.base64
import com.tonapps.blockchain.ton.extensions.hex
import com.tonapps.icu.Coins
import com.tonapps.network.postJSON
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.withRetry
import com.tonapps.wallet.data.account.AccountRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.battery.entity.BatteryConfigEntity
import com.tonapps.wallet.data.battery.entity.BatteryEmulationResult
import com.tonapps.wallet.data.battery.entity.BatteryEntity
import com.tonapps.wallet.data.battery.entity.BatterySupportedTransaction
import com.tonapps.wallet.data.battery.source.LocalConfigDataSource
import com.tonapps.wallet.data.battery.source.LocalDataSource
import com.tonapps.wallet.data.battery.source.RemoteConfigDataSource
import com.tonapps.wallet.data.battery.source.RemoteDataSource
import io.batteryapi.apis.BatteryApi.UnitsGetBalance
import io.batteryapi.models.EmulateMessageToWalletRequest
import io.ktor.util.date.getTimeMillis
import io.tonapi.infrastructure.Serializer
import io.tonapi.models.MessageConsequences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ton.cell.Cell
import java.math.BigDecimal
import java.math.RoundingMode

class BatteryRepository(
    context: Context,
    scope: CoroutineScope,
    private val accountRepository: AccountRepository,
    private val api: API
) {
    private val localDataSource = LocalDataSource(context)
    private val remoteDataSource = RemoteDataSource(api)

    private val localConfigDataSource = LocalConfigDataSource(context)
    private val remoteConfigDataSource = RemoteConfigDataSource(api)

    private val _balanceFlow = MutableStateFlow(getTimeMillis())
    val balanceFlow = _balanceFlow.shareIn(scope, SharingStarted.WhileSubscribed(), 1)

    init {
        scope.launch(Dispatchers.Main) {
            getConfig(testnet = false, ignoreCache = true)
        }
    }

    fun getSupportedTransactions(accountId: String): Map<BatterySupportedTransaction, Boolean> {
        val supportedTransactions = localConfigDataSource.getSupportedTransactions(accountId)
        return supportedTransactions
    }

    fun isSupportedTransaction(
        accountId: String, transaction: BatterySupportedTransaction?
    ): Boolean {
        return getSupportedTransactions(accountId)[transaction] ?: false
    }

    fun setSupportedTransaction(
        accountId: String, transaction: BatterySupportedTransaction, enabled: Boolean
    ): Map<BatterySupportedTransaction, Boolean> {
        val supportedTransactions = getSupportedTransactions(accountId).toMutableMap()
        supportedTransactions[transaction] = enabled
        localConfigDataSource.saveSupportedTransaction(accountId, supportedTransactions)

        return supportedTransactions
    }

    suspend fun getConfig(testnet: Boolean, ignoreCache: Boolean = false): BatteryConfigEntity =
        withContext(Dispatchers.IO) {
            val cacheKey = if (testnet) "testnet" else "mainnet"
            val local: BatteryConfigEntity? = localConfigDataSource.getCache(cacheKey)
            if (local == null || ignoreCache) {
                val remote = remoteConfigDataSource.load(testnet) ?: return@withContext local
                    ?: BatteryConfigEntity.Empty
                localConfigDataSource.setCache(cacheKey, remote)
                return@withContext remote
            }
            return@withContext local
        }

    suspend fun getBalance(
        wallet: WalletEntity,
        ignoreCache: Boolean = false,
    ): BatteryEntity = withContext(Dispatchers.IO) {
        val cacheKey = cacheKey(wallet.publicKey.hex(), wallet.testnet)
        val local: BatteryEntity? = localDataSource.getCache(cacheKey)
        if (local == null || ignoreCache) {
            val token = accountRepository.requestTonProofToken(wallet)
                ?: return@withContext BatteryEntity.Empty
            val remote = remoteDataSource.load(token, wallet.testnet) ?: return@withContext local
                ?: BatteryEntity.Empty

            localDataSource.setCache(cacheKey, remote)
            _balanceFlow.tryEmit(getTimeMillis())
            return@withContext remote
        }
        return@withContext local
    }

    suspend fun emulate(
        wallet: WalletEntity,
        boc: String,
        forceRelayer: Boolean = false
    ): BatteryEmulationResult =
        withContext(Dispatchers.IO) {
            if (api.config.batteryDisabled || api.config.batterySendDisabled) {
                throw IllegalStateException("Battery is disabled")
            }

            if (!forceRelayer && getBalance(wallet).balance.isZero) {
                throw IllegalStateException("Zero balance")
            }

            val token = accountRepository.requestTonProofToken(wallet)
                ?: throw IllegalStateException("can't find TonProof token")

            val host = if (wallet.testnet) api.config.batteryTestnetHost else api.config.batteryHost
            val url = "$host/wallet/emulate"
            val data = "{\"boc\":\"$boc\"}"
            val headers = ArrayMap<String, String>().apply {
                put("X-TonConnect-Auth", token)
            }
            val response = api.defaultHttpClient.postJSON(url, data, headers)
            val responseBody = response.body?.string() ?: throw Exception("empty response")
            val jsonAdapter = Serializer.moshi.adapter(MessageConsequences::class.java)
            val consequences: MessageConsequences =
                jsonAdapter.fromJson(responseBody) ?: throw Exception("empty response")
            val withBattery =
                response.headers["supported-by-battery"] == "true" && response.headers["allowed-by-battery"] == "true"

            BatteryEmulationResult(consequences, withBattery)
        }

    suspend fun emulate(wallet: WalletEntity, message: Cell, forceRelayer: Boolean = false) =
        emulate(wallet, message.base64(), forceRelayer)

    suspend fun sendToRelayer(wallet: WalletEntity, boc: String): Boolean =
        withContext(Dispatchers.IO) {
            if (api.config.batteryDisabled || api.config.batterySendDisabled || !api.isOkStatus(
                    wallet.testnet
                )
            ) {
                return@withContext false
            }
            val token = accountRepository.requestTonProofToken(wallet)
                ?: return@withContext false
            val request = EmulateMessageToWalletRequest(boc = boc)
            val result = withRetry {
                api.battery(wallet.testnet).sendMessage(token, request)
                true
            } ?: false
            getBalance(wallet, true)

            result
        }

    suspend fun sendToRelayer(wallet: WalletEntity, message: Cell) =
        sendToRelayer(wallet, message.base64())

    private fun cacheKey(accountId: String, testnet: Boolean): String {
        if (!testnet) {
            return accountId
        }
        return "${accountId}_testnet_2"
    }

    companion object {
        fun convertToCharges(balance: Coins, meanFees: String): Int {
            val meanFeesBigDecimal = BigDecimal(meanFees)
            return balance.value.divide(meanFeesBigDecimal, 0, RoundingMode.UP).toInt()
        }

        fun calculateChargesAmount(transactionCost: String, meanFees: String): Int {
            val meanFeesBigDecimal = BigDecimal(meanFees)
            val transactionCostBigDecimal = BigDecimal(transactionCost)

            return transactionCostBigDecimal.divide(meanFeesBigDecimal, 0, RoundingMode.HALF_UP)
                .toInt()
        }
    }
}