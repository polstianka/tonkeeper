package com.tonapps.wallet.data.pools.entities

import android.os.Parcelable
import com.tonapps.wallet.api.entity.pool.PoolEntity
import com.tonapps.wallet.api.entity.pool.PoolImplementationEntity
import io.tonapi.models.PoolImplementationType
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class StakingPoolImplementationExt(
    val implementation: PoolImplementationEntity,
    val pools: List<PoolEntity>,
    val maxApy: BigDecimal,
    val minStake: Long
) : Parcelable {

    val isLiquid: Boolean get() = implementation.type.value == PoolImplementationType.liquidTF.value
}
