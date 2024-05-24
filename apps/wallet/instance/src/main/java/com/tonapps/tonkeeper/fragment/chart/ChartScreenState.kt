package com.tonapps.tonkeeper.fragment.chart

import android.net.Uri
import com.tonapps.tonkeeper.api.chart.ChartEntity
import com.tonapps.tonkeeper.api.chart.ChartPeriod
import com.tonapps.tonkeeper.core.history.list.item.HistoryItem
import com.tonapps.tonkeeper.fragment.chart.list.ChartItem
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletType
import uikit.mvi.AsyncState
import uikit.mvi.UiState

data class ChartScreenState(
    val disableSwap: Boolean = false,
    val disableBuyOrSell: Boolean = false,
    val swapUri: Uri = Uri.EMPTY,
    val address: String = "",
    val walletType: WalletType = WalletType.Default,
    val asyncState: AsyncState = AsyncState.Loading,
    val chart: List<ChartEntity> = emptyList(),
    val chartPeriod: ChartPeriod = ChartPeriod.week,
    val balance: CharSequence = "",
    val currencyBalance: CharSequence = "",
    val rateFormat: CharSequence = "",
    val rate24h: String = "",
    val historyItems: List<HistoryItem> = emptyList(),
    val loadedAll: Boolean = false,
): UiState() {


    fun getTopItems(): List<ChartItem> {
        val items = mutableListOf<ChartItem>()
        items.add(ChartItem.Header(balance, currencyBalance, TokenEntity.TON.imageUri.toString()))
        // items.add(ChartItem.Divider)
        items.add(ChartItem.Actions(swapUri, address, walletType, disableSwap, disableBuyOrSell))
        // items.add(ChartItem.Divider)
        items.add(ChartItem.Price(rateFormat, rate24h))
        items.add(ChartItem.Chart(chartPeriod, chart))
        items.add(ChartItem.Period)
        items.add(ChartItem.Divider)
        return items
    }
}