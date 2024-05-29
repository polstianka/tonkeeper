package com.tonapps.wallet.data.pools.entities

import android.os.Parcelable
import com.tonapps.wallet.api.entity.pool.PoolEntity
import com.tonapps.wallet.api.entity.pool.PoolImplementationEntity
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import kotlin.math.min

@Parcelize
data class StakingPoolsEntity(
    private val implementationsList: List<PoolImplementationEntity>,
    private val poolsList: List<PoolEntity>,
) : Parcelable {

    @IgnoredOnParcel
    val implementations: Map<String, StakingPoolImplementationExt>

    @IgnoredOnParcel
    val pools: Map<String, PoolEntity>

    @IgnoredOnParcel
    val maxApyPool: PoolEntity?

    @IgnoredOnParcel
    val liquidJettons: Set<String>

    init {
        val result = mutableMapOf<String, StakingPoolImplementationExt>()
        var maxApyTotal = BigDecimal.ZERO
        var maxApyPool: PoolEntity? = null
        val liquidJettons = mutableSetOf<String>()

        for (implementation in implementationsList) {
            val implPools = poolsList.filter { it.implementation.type == implementation.type }
            var minStake = Long.MAX_VALUE
            var maxApy = BigDecimal.ZERO

            for (pool in implPools) {
                minStake = min(minStake, pool.minStake)
                if (pool.apy > maxApy) {
                    maxApy = pool.apy
                }

                pool.liquidJettonMaster?.let { jetton ->
                    liquidJettons.add(jetton)
                }

                if (maxApy > maxApyTotal) {
                    maxApyTotal = maxApy
                    maxApyPool = pool
                }
            }

            result[implementation.type.value] = StakingPoolImplementationExt(
                implementation = implementation,
                pools = implPools,
                maxApy = maxApy,
                minStake = minStake
            )
        }

        this.implementations = result
        this.pools = poolsList.associateBy { it.address }
        this.maxApyPool = maxApyPool
        this.liquidJettons = liquidJettons
    }
}
