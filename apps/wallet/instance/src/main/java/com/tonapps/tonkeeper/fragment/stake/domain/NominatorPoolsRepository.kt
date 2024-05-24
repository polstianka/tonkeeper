package com.tonapps.tonkeeper.fragment.stake.domain

import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.fragment.stake.domain.model.NominatorPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.wallet.api.API
import io.tonapi.models.AccountStakingInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

class NominatorPoolsRepository(
    private val api: API,
    private val stakingServicesRepository: StakingServicesRepository
) {


    private val nominatorPoolsFlows = mutableMapOf<String, MutableStateFlow<List<NominatorPool>>>()
    private val nominatorPoolsLock = ReentrantReadWriteLock()

    private fun getNominatorPoolsMutableFlow(key: String) = nominatorPoolsLock.write {
        if (!nominatorPoolsFlows.containsKey(key)) {
            nominatorPoolsFlows[key] = MutableStateFlow(emptyList())
        }
        nominatorPoolsFlows[key]!!
    }

    fun getNominatorPoolsFlow(
        walletAddress: String,
        testnet: Boolean
    ): Flow<List<NominatorPool>> {
        val key = getNominatorPoolKey(walletAddress, testnet)
        return getNominatorPoolsMutableFlow(key)
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

    suspend fun loadNominatorPools(walletAddress: String, testnet: Boolean) {
        stakingServicesRepository.loadStakingPools(walletAddress, testnet)
        val stakingPools = stakingServicesRepository.getStakingServicesFlow(testnet, walletAddress)
            .first()
            .flatMap { it.pools }
        val key = getNominatorPoolKey(walletAddress, testnet)
        val stateFlow = getNominatorPoolsMutableFlow(key)
        stateFlow.value = getNominatorPools(walletAddress, testnet, stakingPools)
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

    private fun getNominatorPoolKey(
        walletAddress: String,
        testnet: Boolean
    ) = "$walletAddress$testnet"
}