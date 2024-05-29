package com.tonapps.wallet.data.token.entities

import android.os.Parcelable
import com.tonapps.wallet.api.entity.pool.PoolStakeEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class StakesListEntity(
    val list: List<PoolStakeEntity>
): Parcelable