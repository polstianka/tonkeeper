package com.tonapps.tonkeeper.fragment.swap.domain

import android.net.Uri
import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetBalance
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetRate
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetType
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSimulation
import com.tonapps.tonkeeper.fragment.swap.domain.model.getRecommendedGasValues
import com.tonapps.wallet.api.StonfiAPI
import com.tonapps.wallet.api.entity.BalanceEntity
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.rates.entity.RatesEntity
import com.tonapps.wallet.data.token.TokenRepository
import io.stonfiapi.models.AssetInfoSchema
import io.stonfiapi.models.DexReverseSimulateSwap200Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

class DexAssetsRepository(
    private val api: StonfiAPI,
    private val ratesRepository: RatesRepository,
    private val tokenRepository: TokenRepository,
    private val coroutineScope: CoroutineScope
) {

    private val jettonBalancesLock = ReentrantReadWriteLock()
    private val jettonBalancesFlows = mutableMapOf<String, MutableStateFlow<List<BalanceEntity>>>()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean>
        get() = _isLoading
    private val entitiesFlow = MutableStateFlow<List<DexAssetRate>>(emptyList())

    private val totalBalancesFlows = mutableMapOf<String, Flow<List<DexAssetBalance>>>()
    private val totalBalancesLock = ReentrantReadWriteLock()
    private fun key(walletAddress: String, testnet: Boolean, currency: WalletCurrency): String {
        return "$walletAddress;$testnet;$currency"
    }

    fun getTotalBalancesFlow(
        walletAddress: String,
        testnet: Boolean,
        currency: WalletCurrency
    ): Flow<List<DexAssetBalance>> = totalBalancesLock.write {
        val key = key(walletAddress, testnet, currency)
        if (!totalBalancesFlows.containsKey(key)) {
            val result = buildTotalBalancesFlow(walletAddress, testnet, currency)
                .filter { it.isNotEmpty() }
                .shareIn(coroutineScope, SharingStarted.Eagerly, replay = 1)
            totalBalancesFlows[key] = result
        }
        totalBalancesFlows[key]!!
    }

    private fun buildTotalBalancesFlow(
        walletAddress: String,
        testnet: Boolean,
        currency: WalletCurrency
    ): Flow<List<DexAssetBalance>> {
        val positiveBalanceFlow = getPositiveBalanceFlow(walletAddress, testnet, currency)
        return combine(positiveBalanceFlow, entitiesFlow) { positiveBalances, entities ->
            ratesRepository.load(WalletCurrency.DEFAULT, "TON")
            ratesRepository.load(currency, "TON")
            val tonToUsd = ratesRepository.getRates(WalletCurrency.DEFAULT, "TON")
                .getRate("TON")
            val tonToCurrency = ratesRepository.getRates(currency, "TON")
                .getRate("TON")

            entities.asSequence()
                .map { entity ->
                    val positiveBalance = positiveBalances.firstOrNull {
                        it.rate.tokenEntity.hasTheSameAddress(entity.tokenEntity)
                    }
                    val updatedRate = entity.rate / tonToUsd * tonToCurrency
                    val rate = entity.copy(currency = currency, rate = updatedRate)
                    DexAssetBalance(
                        type = positiveBalance?.type ?: DexAssetType.JETTON, //todo
                        balance = positiveBalance?.balance ?: BigDecimal.ZERO,
                        rate = rate
                    )
                }
                .sortedWith(dexAssetBalanceComparator)
                .toList()
        }
    }

    private val positiveBalanceFlows = mutableMapOf<String, Flow<List<DexAssetBalance>>>()
    private val positiveBalanceLock = ReentrantReadWriteLock()
    fun getPositiveBalanceFlow(
        walletAddress: String,
        testnet: Boolean,
        currency: WalletCurrency
    ): Flow<List<DexAssetBalance>> = positiveBalanceLock.write {
        val key = key(walletAddress, testnet, currency)
        if (!positiveBalanceFlows.containsKey(key)) {
            positiveBalanceFlows[key] = getJettonBalancesMutableFlow(walletAddress, testnet)
                .map { collectBalances(it, currency) }
                .flowOn(Dispatchers.IO)
                .shareIn(coroutineScope, SharingStarted.Eagerly, replay = 1)
        }
        positiveBalanceFlows[key]!!
    }

    private fun key(walletAddress: String, testnet: Boolean) = "$walletAddress;$testnet"

    private fun getJettonBalancesMutableFlow(
        walletAddress: String,
        testnet: Boolean
    ) = jettonBalancesLock.write {
        val key = key(walletAddress, testnet)
        if (!jettonBalancesFlows.containsKey(key)) {
            jettonBalancesFlows[key] = MutableStateFlow(emptyList())
        }
        jettonBalancesFlows[key]!!
    }

    private suspend fun collectBalances(
        jettonBalances: List<BalanceEntity>,
        currency: WalletCurrency
    ): List<DexAssetBalance> {
        val tokenAddresses = jettonBalances.map { it.token.address }
            .toMutableList()
        val rates = ratesRepository.getRates(currency, tokenAddresses)
        return jettonBalances.map { it.toDomain(rates) }
    }

    suspend fun loadBalances(
        walletAddress: String,
        currency: WalletCurrency,
        testnet: Boolean
    ) = withContext(Dispatchers.IO) {
        getJettonBalancesMutableFlow(walletAddress, testnet).apply {
            value = tokenRepository.load(WalletCurrency.DEFAULT, walletAddress, testnet)
        }
        getTotalBalancesFlow(walletAddress, testnet, currency)
    }

    private fun BalanceEntity.toDomain(
        rates: RatesEntity
    ): DexAssetBalance {
        val entity = token
        val rate = DexAssetRate(
            tokenEntity = entity,
            currency = rates.currency,
            rate = rates.getRate(entity.address)
        )
        return DexAssetBalance(
            type = type(),
            balance = value,
            rate = rate
        )
    }

    private fun BalanceEntity.type(): DexAssetType {
        return when {
            token.symbol == "WTON" -> DexAssetType.WTON
            token.symbol == "TON" -> DexAssetType.TON
            else -> DexAssetType.JETTON
        }
    }

    suspend fun loadAssets() = withContext(Dispatchers.IO) {
        _isLoading.value = entitiesFlow.value.isEmpty()
        val response = api.dex.getAssetList()
        entitiesFlow.value = response.assetList.asSequence()
            .filter { it.isValid() }
            .map { it.toUsdRate() }
            .sortedWith(dexAssetRateComparator)
            .toList()

        _isLoading.value = false
    }

    private val dexAssetBalanceComparator = Comparator<DexAssetBalance> { o1, o2 ->
        val f1 = o1.balance * o1.rate.rate
        val f2 = o2.balance * o2.rate.rate
        f2.compareTo(f1)
            .takeUnless { it == 0 }
            ?: dexAssetRateComparator.compare(o1.rate, o2.rate)
    }
    private val dexAssetRateComparator = Comparator<DexAssetRate> { o1, o2 ->
        o1.tokenEntity.name.compareTo(o2.tokenEntity.name)
    }


    suspend fun emulateSwap(
        sendAsset: DexAssetBalance,
        receiveAsset: DexAssetBalance,
        amount: BigDecimal,
        slippageTolerancePercent: Int
    ) = flow {
        emit(SwapSimulation.Loading)
        kotlinx.coroutines.delay(1000L)
        val result = withContext(Dispatchers.IO) {
            api.dex.dexSimulateSwap(
                sendAsset.contractAddress,
                receiveAsset.contractAddress,
                amount.movePointRight(sendAsset.decimals)
                    .setScale(0, RoundingMode.FLOOR)
                    .toPlainString(),
                BigDecimal(slippageTolerancePercent).movePointLeft(2).toPlainString()
            )
        }
        emit(result.toBalance(sendAsset, receiveAsset))
    }

    private fun DexReverseSimulateSwap200Response.toBalance(
        sentAsset: DexAssetBalance,
        receiveAsset: DexAssetBalance
    ): SwapSimulation.Result {
        return SwapSimulation.Result(
            exchangeRate = BigDecimal(swapRate),
            priceImpact = BigDecimal(priceImpact),
            minimumReceivedAmount = Coin.toCoins(minAskUnits, receiveAsset.decimals),
            receivedAsset = receiveAsset,
            liquidityProviderFee = Coin.toCoins(feeUnits, receiveAsset.decimals),
            sentAsset = sentAsset,
            blockchainFee = sentAsset.getRecommendedGasValues(receiveAsset)
        )
    }


    private fun AssetInfoSchema.isValid(): Boolean {
        return dexPriceUsd != null &&
                !blacklisted &&
                !deprecated &&
                !community &&
                imageUrl?.isNotBlank() == true &&
                displayName?.isNotBlank() == true
    }

    private fun AssetInfoSchema.toUsdRate(): DexAssetRate {
        return DexAssetRate(
            tokenEntity = toTokenEntity(),
            currency = WalletCurrency.DEFAULT,
            rate = BigDecimal(dexPriceUsd)
        )
    }

    private fun AssetInfoSchema.toTokenEntity(): TokenEntity {
        return TokenEntity(
            address = contractAddress,
            name = displayName!!,
            symbol = symbol,
            imageUri = Uri.parse(imageUrl!!),
            decimals = decimals,
            verification = verification()
        )
    }

    private fun AssetInfoSchema.verification(): TokenEntity.Verification {
        return if (community) {
            TokenEntity.Verification.whitelist
        } else {
            TokenEntity.Verification.none
        }
    }
}