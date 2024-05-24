package com.tonapps.tonkeeper.fragment.stake.domain

import android.net.Uri
import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.fragment.stake.domain.model.NominatorPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakedBalance
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakedLiquidBalance
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPoolLiquidJetton
import com.tonapps.tonkeeper.fragment.swap.domain.DexAssetsRepository
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.rates.entity.RateEntity
import com.tonapps.wallet.data.rates.entity.RatesEntity
import io.tonapi.models.AccountStakingInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.ton.block.MsgAddressInt
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

class StakingRepository(
    private val api: API,
    private val ratesRepository: RatesRepository,
    private val dexAssetsRepository: DexAssetsRepository,
    private val stakingServicesRepository: StakingServicesRepository
) {
    private val stakedBalancesLock = ReentrantReadWriteLock()
    private val stakedBalancesFlows = mutableMapOf<String, MutableStateFlow<List<StakedBalance>>>()

    private val nominatorPoolsFlows = mutableMapOf<String, MutableStateFlow<List<NominatorPool>>>()
    private val nominatorPoolsLock = ReentrantReadWriteLock()

    suspend fun loadNominatorPools(walletAddress: String, testnet: Boolean) {
        stakingServicesRepository.loadStakingPools(walletAddress, testnet)
        val stakingPools = stakingServicesRepository.getStakingServicesFlow(testnet, walletAddress)
            .first()
            .flatMap { it.pools }
        val key = getNominatorPoolKey(walletAddress, testnet)
        val stateFlow = getNominatorPoolsMutableFlow(key)
        stateFlow.value = getNominatorPools(walletAddress, testnet, stakingPools)
    }

    suspend fun getJetton(
        masterAddress: String,
        poolName: String,
        currency: WalletCurrency,
        testnet: Boolean
    ): StakingPoolLiquidJetton = withContext(Dispatchers.IO) {
        val deferredJettonInfo = async { api.jettons(testnet).getJettonInfo(masterAddress) }
        val deferredRate = async { getRate(currency, masterAddress) }
        val jettonInfo = deferredJettonInfo.await()
        val rate = deferredRate.await()


        StakingPoolLiquidJetton(
            address = masterAddress,
            iconUri = jettonInfo.metadata.image?.let { Uri.parse(it) } ?: Uri.EMPTY,
            symbol = jettonInfo.metadata.symbol,
            price = rate?.value,
            poolName = poolName,
            currency = currency
        )
    }

    private fun getNominatorPoolsFlow(walletAddress: String, testnet: Boolean): Flow<List<NominatorPool>> {
        val key = getNominatorPoolKey(walletAddress, testnet)
        return getNominatorPoolsMutableFlow(key)
    }

    private fun getNominatorPoolsMutableFlow(key: String) = nominatorPoolsLock.write {
        if (!nominatorPoolsFlows.containsKey(key)) {
            nominatorPoolsFlows[key] = MutableStateFlow(emptyList())
        }
        nominatorPoolsFlows[key]!!
    }

    private suspend fun getNominatorPools(
        walletAddress: String,
        testnet: Boolean,
        pools: List<StakingPool>
    ): List<NominatorPool> = withContext(Dispatchers.IO) {
        api.staking(testnet)
            .getAccountNominatorsPools(walletAddress)
            .pools.mapNotNull { it.toDomain(pools) }
    }

    private fun AccountStakingInfo.toDomain(
        pools: List<StakingPool>
    ): NominatorPool? {
        val stakingPool = pools.firstOrNull { it.address == pool }
            ?: return null
        return NominatorPool(
            stakingPool = stakingPool,
            amount = Coin.toCoins(amount),
            pendingDeposit = Coin.toCoins(pendingDeposit),
            pendingWithdraw = Coin.toCoins(pendingWithdraw),
            readyWithdraw = Coin.toCoins(readyWithdraw)
        )
    }

    private suspend fun getRate(
        currency: WalletCurrency,
        masterAddress: String
    ): RateEntity? {
        val cachedValue = ratesRepository.cache(currency, listOf(masterAddress))
            .rate(masterAddress)
        return cachedValue ?: ratesRepository.getRates(currency, masterAddress)
            .rate(masterAddress)
    }

    suspend fun loadStakedBalances(
        walletAddress: String,
        currency: WalletCurrency,
        testnet: Boolean
    ) = withContext(Dispatchers.IO) {
        val key = getStakedBalanceKey(walletAddress, currency, testnet)
        val stateFlow = getStakedBalance(key)

        val poolsDeferred = async {
            stakingServicesRepository.loadStakingPools(walletAddress, testnet)
            stakingServicesRepository.getStakingServicesFlow(testnet, walletAddress)
                .first()
        }
        val jettonBalancesDeferred = async {
            dexAssetsRepository.getIsLoadingFlow(walletAddress)
                .filter { !it }
                .first()
            dexAssetsRepository.getPositiveBalanceFlow(walletAddress)
                .first()
        }
        val nominatorPoolsDeferred = async {
            loadNominatorPools(walletAddress, testnet)
            getNominatorPoolKey(walletAddress, testnet)
                .let { getNominatorPoolsMutableFlow(it) }
                .value
        }

        val stakingServices = poolsDeferred.await()
        val jettonBalances = jettonBalancesDeferred.await()
        val nominatorPools = nominatorPoolsDeferred.await()

        val pools = stakingServices.flatMap { it.pools }
        val poolsWithJettons = pools.filter { it.liquidJettonMaster != null }
        val tokens = jettonBalances.map { it.contractAddress }
            .toMutableList()
            .apply { addAll(poolsWithJettons.mapNotNull { it.liquidJettonMaster }) }
            .apply { add("TON") }
        ratesRepository.load(currency, tokens)
        val rates = ratesRepository.getRates(currency, tokens)
        val tonRate = rates.rate("TON")!!

        val activePoolAddresses = mutableSetOf<String>()
        nominatorPools.forEach { activePoolAddresses.add(it.stakingPool.address) }
        poolsWithJettons.filter { pool ->
            val poolAddress = MsgAddressInt.parse(pool.liquidJettonMaster!!)
            jettonBalances.any { poolAddress.isAddressEqual(it.contractAddress) }
        }.forEach { activePoolAddresses.add(it.address) }

        stateFlow.value = activePoolAddresses.map { poolAddress ->
            val pool = pools.first { it.address == poolAddress }
            val service = stakingServices.first { it.type == pool.serviceType }
            val liquidBalance = liquidBalance(pool, jettonBalances, rates)
            val solidBalance = nominatorPools.firstOrNull { it.stakingPool.address == poolAddress }
            StakedBalance(
                pool = pool,
                service = service,
                liquidBalance = liquidBalance,
                solidBalance = solidBalance,
                fiatCurrency = currency,
                tonRate = tonRate
            )
        }
    }

    private fun liquidBalance(
        pool: StakingPool,
        jettonBalances: List<DexAsset>,
        rates: RatesEntity
    ): StakedLiquidBalance? {
        val address = pool.liquidJettonMaster ?: return null
        val addressMAI = MsgAddressInt.parse(address)
        val jetton = jettonBalances.firstOrNull {
            addressMAI.isAddressEqual(it.contractAddress)
        } ?: return null
        return jetton.stakedLiquidBalance(rates)
    }

    private fun DexAsset.stakedLiquidBalance(
        rates: RatesEntity
    ): StakedLiquidBalance {
        return StakedLiquidBalance(
            asset = this,
            assetRate = rates.rate(contractAddress)!!,
        )
    }

    fun getStakedBalanceFlow(
        walletAddress: String,
        currency: WalletCurrency,
        testnet: Boolean
    ): Flow<List<StakedBalance>> {
        val key = getStakedBalanceKey(walletAddress, currency, testnet)
        return getStakedBalance(key)
    }

    private fun getStakedBalance(
        key: String
    ): MutableStateFlow<List<StakedBalance>> = stakedBalancesLock.write {
        if (stakedBalancesFlows.containsKey(key)) {
            stakedBalancesFlows[key]!!
        } else {
            val result = MutableStateFlow<List<StakedBalance>>(emptyList())
            stakedBalancesFlows[key] = result
            result
        }
    }

    private fun getStakedBalanceKey(
        walletAddress: String,
        currency: WalletCurrency,
        testnet: Boolean
    ) = "$walletAddress${currency.code}$testnet"

    private fun getNominatorPoolKey(
        walletAddress: String,
        testnet: Boolean
    ) = "$walletAddress$testnet"
}

fun String.isAddressEqual(another: String): Boolean {
    return MsgAddressInt.parse(this).isAddressEqual(another)
}

fun MsgAddressInt.isAddressEqual(another: String): Boolean {
    return this == MsgAddressInt.parse(another)
}