package com.tonapps.tonkeeper.fragment.stake.domain

import com.tonapps.tonkeeper.fragment.stake.data.mapper.StakingServiceMapper
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPoolLiquidJetton
import com.tonapps.tonkeeper.fragment.stake.domain.model.maxAPY
import com.tonapps.wallet.api.API
import io.tonapi.models.PoolImplementationType
import io.tonapi.models.PoolInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class StakingRepository(
    private val api: API,
    private val mapper: StakingServiceMapper
) {

    // todo add caching
    suspend fun getStakingPools(
        accountId: String,
        testnet: Boolean
    ) = withContext(Dispatchers.IO) {
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

    suspend fun getJetton(
        masterAddress: String,
        poolName: String,
        testnet: Boolean
    ): StakingPoolLiquidJetton = withContext(Dispatchers.IO) {
        val response = api.jettons(testnet)
            .getJettonInfo(masterAddress)

        StakingPoolLiquidJetton(
            address = masterAddress,
            iconUrl = response.metadata.image ?: "",
            symbol = response.metadata.symbol,
            price = BigDecimal.ONE, // todo
            poolName = poolName
        )
    }
}