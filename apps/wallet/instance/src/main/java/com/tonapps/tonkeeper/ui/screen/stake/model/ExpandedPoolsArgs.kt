package com.tonapps.tonkeeper.ui.screen.stake.model

import android.os.Parcelable
import com.tonapps.wallet.api.entity.StakePoolsEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExpandedPoolsArgs(
    val name: String,
    val type: StakePoolsEntity.PoolImplementationType,
    val maxApyAddress: String,
) : Parcelable