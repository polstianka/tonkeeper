package com.tonapps.tonkeeper.ui.screen.stake.model

import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.StakePoolsEntity

data class PoolModel(
    val address: String,
    val name: String,
    val apyFormatted: String,
    val isMaxApy: Boolean,
    val implType: StakePoolsEntity.PoolImplementationType,
    val position: ListCell.Position = ListCell.Position.SINGLE,
    val minStake: Long,
    val links: List<String>,
    val selected: Boolean
) : BaseListItem()

val StakePoolsEntity.PoolImplementationType.icon: Int
    get() {
        return when (this) {
            StakePoolsEntity.PoolImplementationType.tf -> R.drawable.ic_staking_tf
            StakePoolsEntity.PoolImplementationType.whales -> R.drawable.ic_staking_whales
            StakePoolsEntity.PoolImplementationType.liquidTF -> R.drawable.ic_staking_tonstakers
        }
    }

val StakePoolsEntity.PoolImplementationType.iconURL: String
    get() = "res:/${icon}"
