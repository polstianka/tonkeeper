package com.tonapps.tonkeeper.fragment.stake.domain

import com.tonapps.tonkeeper.fragment.stake.data.mapper.StakingServiceMapper
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import com.tonapps.tonkeeper.fragment.stake.domain.model.maxAPY
import com.tonapps.wallet.api.API
import io.tonapi.models.PoolImplementationType
import io.tonapi.models.PoolInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

class StakingServicesRepository(
    private val api: API,
    private val mapper: StakingServiceMapper,
) {

    private val loadLock = ReentrantReadWriteLock()
    private val createLock = ReentrantReadWriteLock()
    private val stakingPools = mutableMapOf<Boolean, MutableStateFlow<List<StakingService>>>()

    fun getStakingServicesFlow(
        testnet: Boolean,
        walletAddress: String
    ): Flow<List<StakingService>> {
        return getStakingPoolsMutableFlow(testnet)
    }

    private fun getStakingPoolsMutableFlow(
        testnet: Boolean
    ): MutableStateFlow<List<StakingService>> = createLock.write {
        if (!stakingPools.containsKey(testnet)) {
            stakingPools[testnet] = MutableStateFlow(emptyList())
        }
        stakingPools[testnet]!!
    }

    suspend fun loadStakingPools(
        accountId: String,
        testnet: Boolean
    ) = loadLock.write {
        val flow = getStakingPoolsMutableFlow(testnet)
        if (flow.value.isNotEmpty()) return

        flow.value = getStakingServices(accountId, testnet)
    }

    private suspend fun getStakingServices(
        accountId: String,
        testnet: Boolean
    ): List<StakingService> = withContext(Dispatchers.IO) {
        val result = api.getStakingPools(accountId, testnet)
        val implementationsByPool = PoolImplementationType.entries
            .associateWith { mutableSetOf<PoolInfo>() }

        result.pools.forEach { pool ->
            implementationsByPool[pool.implementation]?.add(pool)
        }

        implementationsByPool.asSequence()
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