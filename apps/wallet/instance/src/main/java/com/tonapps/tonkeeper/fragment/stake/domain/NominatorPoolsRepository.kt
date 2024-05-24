package com.tonapps.tonkeeper.fragment.stake.domain

import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.fragment.stake.domain.model.NominatorPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.wallet.api.API
import io.tonapi.infrastructure.ClientException
import io.tonapi.models.AccountStakingInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

class NominatorPoolsRepository(
    private val api: API,
    private val repository: StakingServicesRepository
) {
    private val stakingInfosFlows =
        mutableMapOf<String, MutableStateFlow<List<AccountStakingInfo>>>()
    private val lock = ReentrantReadWriteLock()

    fun getNominatorPoolsFlow(
        walletAddress: String,
        testnet: Boolean
    ): Flow<List<NominatorPool>> {
        val key = key(walletAddress, testnet)
        val stakingInfosFlow = getStakingInfosFlow(key)
        val servicesFlow = repository.getStakingServicesFlow(testnet, walletAddress)
        return combine(stakingInfosFlow, servicesFlow) { stakingInfos, services ->
            val pools = services.flatMap { it.pools }
            stakingInfos.mapNotNull { it.toDomain(pools) }
        }
    }

    private fun getStakingInfosFlow(
        key: String
    ): MutableStateFlow<List<AccountStakingInfo>> = lock.write {
        if (!stakingInfosFlows.containsKey(key)) {
            stakingInfosFlows[key] = MutableStateFlow(emptyList())
        }
        stakingInfosFlows[key]!!
    }

    suspend fun loadNominatorPools(walletAddress: String, testnet: Boolean) {
        val key = key(walletAddress, testnet)
        getStakingInfosFlow(key).value = getStakingInfo(walletAddress, testnet)
    }

    private suspend fun getStakingInfo(
        walletAddress: String,
        testnet: Boolean
    ): List<AccountStakingInfo> = try {
        api.staking(testnet)
            .getAccountNominatorsPools(walletAddress)
            .pools
    } catch (serverError: ClientException) {
        emptyList()
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

    private fun key(
        walletAddress: String,
        testnet: Boolean
    ) = "$walletAddress$testnet"
}