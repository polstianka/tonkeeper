package com.tonapps.tonkeeper.ui.screen.stake

import android.content.Context
import com.tonapps.network.Network
import com.tonapps.tonkeeper.api.swap.StonfiSwapAsset
import com.tonapps.tonkeeper.api.withRetry
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.Tonapi
import io.tonapi.models.GetStakingPools200Response
import io.tonapi.models.PoolImplementation
import io.tonapi.models.PoolInfo
import org.json.JSONObject
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap

// todo
class StakingRepository(
    context: Context,
    private val api: API
) {

    companion object {
        var maxApy: BigDecimal = BigDecimal.ZERO
    }

    private val stakingApi = Tonapi.staking
    val pools = mutableListOf<PoolInfo>()
    var implMap: Map<String, PoolImplementation> = mutableMapOf()
    private var cached = ConcurrentHashMap<String, GetStakingPools200Response>()

    suspend fun getPools(testnet: Boolean, accountId: String): GetStakingPools200Response? {
        if (cached[accountId] != null) {
            return cached[accountId]
        }
        val pool = loadPools(testnet, accountId)
        if (pool != null) {
            pools.clear()
            pools.addAll(pool.pools)
            implMap = pool.implementations
            maxApy = pools.maxByOrNull { it.apy }?.apy ?: BigDecimal.ZERO
            cached[accountId] = pool
        }
        return pool
    }

    private suspend fun loadPools(testnet: Boolean, accountId: String): GetStakingPools200Response? {
        return withRetry { stakingApi.get(testnet).getStakingPools(includeUnverified = false, availableFor = accountId) }
    }
}