package com.tonapps.tonkeeper.fragment.stake.domain

import com.tonapps.tonkeeper.fragment.stake.data.mapper.StakingServiceMapper
import com.tonapps.tonkeeper.fragment.stake.domain.model.maxAPY
import com.tonapps.wallet.api.API
import io.tonapi.models.PoolImplementationType
import io.tonapi.models.PoolInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StakingRepository(
    private val api: API,
    private val mapper: StakingServiceMapper
) {

    companion object {
        const val isTestNet = false // todo
    }
    // todo add caching
    suspend fun getStakingPools(accountId: String) = withContext(Dispatchers.IO) {
        val result = api.getStakingPools(accountId, isTestNet)
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