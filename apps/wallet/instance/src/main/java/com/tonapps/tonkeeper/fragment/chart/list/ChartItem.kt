package com.tonapps.tonkeeper.fragment.chart.list

import android.net.Uri
import com.tonapps.tonkeeper.api.chart.ChartEntity
import com.tonapps.tonkeeper.api.chart.ChartPeriod
import com.tonapps.wallet.data.account.WalletType
import io.tonapi.models.JettonBalance

sealed class ChartItem(
    type: Int
): com.tonapps.uikit.list.BaseListItem(type) {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ACTIONS = 1
        const val TYPE_CHART = 2
        const val TYPE_DIVIDER = 3
        const val TYPE_PRICE = 4
        const val TYPE_PERIOD = 5
        const val TYPE_ACTIONS_STAKED = 6
    }

    data class Header(
        val balance: CharSequence,
        val currencyBalance: CharSequence,
        val iconUrl: String,
        val staked: Boolean = false
    ): ChartItem(TYPE_HEADER)

    data class Actions(
        val swapUri: Uri,
        val address: String,
        val walletType: WalletType,
        val disableSwap: Boolean,
        val disableBuyOrSell: Boolean,
    ): ChartItem(TYPE_ACTIONS)

    data class ActionsStaked(
        val wallet: String,
        val jetton: JettonBalance,
        val walletType: WalletType,
        val poolAddress: String,
    ) : ChartItem(TYPE_ACTIONS_STAKED)

    data object Period: ChartItem(TYPE_PERIOD)

    data class Chart(
        val period: ChartPeriod,
        val data: List<ChartEntity>,
        val labels: List<String> = emptyList(),
        val isStaking: Boolean = false,
        val minMaxPrice: List<String> = emptyList()
    ): ChartItem(TYPE_CHART)

    data class Price(
        val rateFormat: CharSequence,
        val rate24h: String
    ): ChartItem(TYPE_PRICE)

    data object Divider: ChartItem(TYPE_DIVIDER)
}