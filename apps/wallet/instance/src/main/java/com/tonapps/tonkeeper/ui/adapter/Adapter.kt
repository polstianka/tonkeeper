package com.tonapps.tonkeeper.ui.adapter

import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.adapter.holder.HolderChartLegend
import com.tonapps.tonkeeper.ui.adapter.holder.HolderDescriptionBody3
import com.tonapps.tonkeeper.ui.adapter.holder.HolderEmpty
import com.tonapps.tonkeeper.ui.adapter.holder.HolderFiatMethod
import com.tonapps.tonkeeper.ui.adapter.holder.HolderLinks
import com.tonapps.tonkeeper.ui.adapter.holder.HolderTitleH3
import com.tonapps.tonkeeper.ui.adapter.holder.HolderTitleLabel1
import com.tonapps.tonkeeper.ui.adapter.holder.HolderToken
import com.tonapps.tonkeeper.ui.adapter.holder.HolderTokenSuggested
import com.tonapps.tonkeeper.ui.adapter.holder.HolderTokenSuggestions
import com.tonapps.tonkeeper.ui.adapter.holder.pools.HolderPool
import com.tonapps.tonkeeper.ui.adapter.holder.pools.HolderPoolImplementation
import com.tonapps.tonkeeper.ui.adapter.holder.staking.HolderStakingPageActions
import com.tonapps.tonkeeper.ui.adapter.holder.staking.HolderStakingPageChartHeader
import com.tonapps.tonkeeper.ui.adapter.holder.staking.HolderStakingPageChartLine
import com.tonapps.tonkeeper.ui.adapter.holder.staking.HolderStakingPageChartPeriod
import com.tonapps.tonkeeper.ui.adapter.holder.staking.HolderStakingPageHeader
import com.tonapps.tonkeeper.ui.adapter.holder.staking.HolderStakingPagePendingAction
import com.tonapps.tonkeeper.ui.adapter.holder.staking.HolderStakingPagePoolInfoRows
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.BaseListItem

open class Adapter: com.tonapps.uikit.list.BaseListAdapter() {
    override fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return when (viewType) {
            Item.Type.LINKS.value -> HolderLinks(parent)
            Item.Type.TOKEN.value -> HolderToken(parent)
            Item.Type.TOKEN_SUGGESTED.value -> HolderTokenSuggested(parent)
            Item.Type.TOKEN_SUGGESTIONS.value -> HolderTokenSuggestions(parent)
            Item.Type.FIAT_METHOD.value -> HolderFiatMethod(parent)

            Item.Type.TITLE_H3.value -> HolderTitleH3(parent)
            Item.Type.TITLE_LABEL1.value -> HolderTitleLabel1(parent)
            Item.Type.DESCRIPTION_BODY3.value -> HolderDescriptionBody3(parent)
            Item.Type.CHART_LEGEND.value -> HolderChartLegend(parent)

            Item.Type.STAKING_PAGE_HEADER.value -> HolderStakingPageHeader(parent)
            Item.Type.STAKING_PAGE_ACTIONS.value -> HolderStakingPageActions(parent)
            Item.Type.STAKING_PAGE_POOL_INFO_ROWS.value -> HolderStakingPagePoolInfoRows(parent)
            Item.Type.STAKING_PAGE_CHART.value -> HolderStakingPageChartLine(parent)
            Item.Type.STAKING_PAGE_CHART_PERIOD.value -> HolderStakingPageChartPeriod(parent)
            Item.Type.STAKING_PAGE_CHART_HEADER.value -> HolderStakingPageChartHeader(parent)
            Item.Type.STAKING_PAGE_PENDING_ACTION.value -> HolderStakingPagePendingAction(parent)

            Item.Type.STAKING_PAGE_POOL_IMPLEMENTATION.value -> HolderPoolImplementation(parent)
            Item.Type.STAKING_PAGE_POOL.value -> HolderPool(parent)

            Item.Type.OFFSET_8DP.value -> HolderEmpty(parent)
            Item.Type.OFFSET_16DP.value -> HolderEmpty(parent)
            Item.Type.OFFSET_32DP.value -> HolderEmpty(parent)

            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }
}