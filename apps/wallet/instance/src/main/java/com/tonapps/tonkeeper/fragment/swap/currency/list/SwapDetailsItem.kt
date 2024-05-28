package com.tonapps.tonkeeper.fragment.swap.currency.list

import androidx.annotation.StringRes

sealed class SwapDetailsItem(
    type: Int
): com.tonapps.uikit.list.BaseListItem(type) {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ACTIONS = 1
        const val TYPE_DIVIDER = 2
    }

    data class Header(
        val title: String,
        val loading: Boolean,
    ): SwapDetailsItem(TYPE_HEADER)

    data class Cell(
        @StringRes
        val title: Int,
        val value: String,
        @StringRes
        val additionalInfo: Int? = null,
    ): SwapDetailsItem(TYPE_ACTIONS)

    data object Divider: SwapDetailsItem(TYPE_DIVIDER)
}