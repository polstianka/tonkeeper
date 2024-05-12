package com.tonapps.tonkeeper.ui.screen.stake.model

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