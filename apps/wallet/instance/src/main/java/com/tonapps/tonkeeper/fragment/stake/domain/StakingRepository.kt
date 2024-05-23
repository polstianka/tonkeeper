package com.tonapps.tonkeeper.fragment.stake.domain

import android.util.Log
import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.fragment.stake.data.mapper.StakingServiceMapper
import com.tonapps.tonkeeper.fragment.stake.domain.model.NominatorPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakedBalance
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakedLiquidBalance
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPoolLiquidJetton
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import com.tonapps.tonkeeper.fragment.stake.domain.model.maxAPY
import com.tonapps.tonkeeper.fragment.swap.domain.DexAssetsRepository
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.rates.entity.RateEntity
import com.tonapps.wallet.data.rates.entity.RatesEntity
import io.tonapi.models.AccountStakingInfo
import io.tonapi.models.PoolImplementationType
import io.tonapi.models.PoolInfo
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
    private val mapper: StakingServiceMapper,
    private val ratesRepository: RatesRepository,
    private val dexAssetsRepository: DexAssetsRepository
) {
    private val stakedBalancesLock = ReentrantReadWriteLock()
    private val stakedBalancesFlows = mutableMapOf<String, MutableStateFlow<List<StakedBalance>>>()

    private val stakingPoolsLock = ReentrantReadWriteLock()
    private val _stakingPools = MutableStateFlow<List<StakingService>>(emptyList())

    private val nominatorPoolsFlows = mutableMapOf<String, MutableStateFlow<List<NominatorPool>>>()
    private val nominatorPoolsLock = ReentrantReadWriteLock()

    val stakingPools: Flow<List<StakingService>>
        get() = _stakingPools

    suspend fun loadNominatorPools(walletAddress: String, testnet: Boolean) {
        loadStakingPools(walletAddress, testnet)
        val stakingPools = _stakingPools.value.flatMap { it.pools }
        val key = getNominatorPoolKey(walletAddress, testnet)
        val stateFlow = getNominatorPoolsMutableFlow(key)
        stateFlow.value = getNominatorPools(walletAddress, testnet, stakingPools)
    }

    suspend fun loadStakingPools(
        accountId: String,
        testnet: Boolean
    ) = withContext(Dispatchers.IO) {
        stakingPoolsLock.write {
            if (_stakingPools.value.isNotEmpty()) return@withContext

            val result = api.getStakingPools(accountId, false)
            val implementationsByPool = PoolImplementationType.entries
                .associateWith { mutableSetOf<PoolInfo>() }

            result.pools.forEach { pool ->
                implementationsByPool[pool.implementation]?.add(pool)
            }

            _stakingPools.value = implementationsByPool.asSequence()
                .filter { it.value.isNotEmpty() }
                .map { mapper.map(it, result.implementations) }
                .sortedByDescending { it.maxAPY }
                .mapIndexed { index1, item1 ->
                    if (index1 == 0) {
                        item1.copy(
                            pools = item1.pools.mapIndexed { index, item ->
                                if (index == 0) {
                                    item.copy(isMaxApy = true)
                                } else {
                                    item
                                }
                            }
                        )
                    } else {
                        item1
                    }
                }
                .toList()
        }
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
            iconUrl = jettonInfo.metadata.image ?: "",
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
            loadStakingPools(walletAddress, testnet)
            _stakingPools.value
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

        Log.wtf("###", "$nominatorPools")

        val pools = stakingServices.flatMap { it.pools }
        val poolsWithJettons = pools.filter { it.liquidJettonMaster != null }
        val tokens = jettonBalances.map { it.contractAddress }
            .toMutableList()
            .apply { addAll(poolsWithJettons.mapNotNull { it.liquidJettonMaster }) }
            .apply { add("TON") }
        ratesRepository.load(currency, tokens)
        val rates = ratesRepository.getRates(currency, tokens)
        val tonRate = rates.rate("TON")!!
        val liquidStaking = poolsWithJettons.mapNotNull { poolWithJetton ->
            val liquidJettonAddressParsed = MsgAddressInt.parse(poolWithJetton.liquidJettonMaster!!)
            val jetton = jettonBalances.firstOrNull {
                liquidJettonAddressParsed.isAddressEqual(it.contractAddress)
            }
            if (jetton == null) {
                null
            } else {
                StakedBalance(
                    poolWithJetton,
                    stakingServices.first { it.type == poolWithJetton.serviceType },
                    liquidBalance = jetton.stakedLiquidBalance(rates),
                    fiatCurrency = currency,
                    tonRate = tonRate
                )
            }
        }
        val normalStaking = nominatorPools.map { pool ->
            StakedBalance(
                pool.stakingPool,
                stakingServices.first { it.type == pool.stakingPool.serviceType },
                liquidBalance = null,
                fiatCurrency = currency,
                tonRate = tonRate
            )
        }
        stateFlow.value = liquidStaking + normalStaking
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