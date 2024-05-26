package com.tonapps.tonkeeper.fragment.stake.domain

import android.net.Uri
import com.tonapps.tonkeeper.fragment.stake.domain.model.NominatorPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakedBalance
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakedLiquidBalance
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPoolLiquidJetton
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import com.tonapps.tonkeeper.fragment.swap.domain.DexAssetsRepository
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetBalance
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.rates.entity.RateEntity
import com.tonapps.wallet.data.rates.entity.RatesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import org.ton.block.MsgAddressInt

class StakingRepository(
    private val api: API,
    private val ratesRepository: RatesRepository,
    private val dexAssetsRepository: DexAssetsRepository,
    private val stakingServicesRepository: StakingServicesRepository,
    private val nominatorPoolsRepository: NominatorPoolsRepository
) {
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
        val a = async { stakingServicesRepository.loadStakingPools(walletAddress, testnet) }
        val b = async { dexAssetsRepository.loadAssets(walletAddress, currency) }
        val c = async { nominatorPoolsRepository.loadNominatorPools(walletAddress, testnet) }
        val d = async { dexAssetsRepository.loadBalances(walletAddress, testnet) }
        listOf(a, b, c, d).forEach { it.await() }
    }

    private suspend fun collectStakedBalances(
        stakingServices: List<StakingService>,
        jettonBalances: List<DexAssetBalance>,
        nominatorPools: List<NominatorPool>,
        currency: WalletCurrency
    ): List<StakedBalance> {
        val pools = stakingServices.flatMap { it.pools }
        val poolsWithJettons = pools.filter { it.liquidJettonMaster != null }
        val rates = ratesEntity(jettonBalances, poolsWithJettons, currency)
        val tonRate = rates.rate("TON")!!

        val activePoolAddresses = mutableSetOf<String>()
        nominatorPools.forEach { activePoolAddresses.add(it.stakingPool.address) }
        poolsWithJettons.filter { pool ->
            val poolAddress = MsgAddressInt.parse(pool.liquidJettonMaster!!)
            jettonBalances.any { poolAddress.isAddressEqual(it.contractAddress) }
        }.forEach { activePoolAddresses.add(it.address) }

        return activePoolAddresses.map { poolAddress ->
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

    private suspend fun ratesEntity(
        jettonBalances: List<DexAssetBalance>,
        poolsWithJettons: List<StakingPool>,
        currency: WalletCurrency
    ): RatesEntity {
        val tokens = jettonBalances.map { it.contractAddress }
            .toMutableList()
            .apply { addAll(poolsWithJettons.mapNotNull { it.liquidJettonMaster }) }
            .apply { add("TON") }
        ratesRepository.load(currency, tokens)
        val rates = ratesRepository.getRates(currency, tokens)
        return rates
    }

    private fun liquidBalance(
        pool: StakingPool,
        jettonBalances: List<DexAssetBalance>,
        rates: RatesEntity
    ): StakedLiquidBalance? {
        val address = pool.liquidJettonMaster ?: return null
        val addressMAI = MsgAddressInt.parse(address)
        val jetton = jettonBalances.firstOrNull {
            addressMAI.isAddressEqual(it.contractAddress)
        } ?: return null
        return jetton.stakedLiquidBalance(rates)
    }

    private fun DexAssetBalance.stakedLiquidBalance(
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
        val stakingServices = stakingServicesRepository.getStakingServicesFlow(
            testnet,
            walletAddress
        )
        val jettonBalances = dexAssetsRepository.getPositiveBalanceFlow(
            walletAddress,
            testnet,
            currency
        )
        val nominatorPools = nominatorPoolsRepository.getNominatorPoolsFlow(walletAddress, testnet)
        return combine(stakingServices, jettonBalances, nominatorPools) { a, b, c ->
            collectStakedBalances(a, b, c, currency)
        }
    }
}

fun String.isAddressEqual(another: String): Boolean {
    return MsgAddressInt.parse(this).isAddressEqual(another)
}

fun MsgAddressInt.isAddressEqual(another: String): Boolean {
    return this == MsgAddressInt.parse(another)
}