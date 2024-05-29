package com.tonapps.tonkeeper.ui.screen.stake.options

import com.tonapps.uikit.list.BaseListItem
import io.tonapi.models.PoolImplementation
import io.tonapi.models.PoolImplementationType
import io.tonapi.models.PoolInfo

sealed class OptionItem(
    type: Int,
) : BaseListItem(type) {

    companion object {
        const val TYPE_OPTION = 1
        const val TYPE_HEADER = 2
        const val TYPE_OPTION_LIST = 3
    }

    data class OptionList(
        val position: Int,
        val count: Int,
        val icon: Int,
        val implementation: PoolImplementation,
        val implementationType: PoolImplementationType
    ) : OptionItem(TYPE_OPTION_LIST)

    data class Header(
        val title: String
    ) : OptionItem(TYPE_HEADER)

    data class Option(
        val position: Int,
        val count: Int,
        val chosen: Boolean,
        val poolInfo: PoolInfo
    ) : OptionItem(TYPE_OPTION)
}