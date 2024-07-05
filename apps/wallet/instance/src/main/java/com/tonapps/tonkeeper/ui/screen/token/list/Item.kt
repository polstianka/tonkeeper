package com.tonapps.tonkeeper.ui.screen.token.list

import android.net.Uri
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.wallet.api.entity.ChartEntity
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletType

sealed class Item(type: Int): BaseListItem(type) {

    companion object {
        const val TYPE_BALANCE = 0
        const val TYPE_ACTIONS = 1
        const val TYPE_PRICE = 2
        const val TYPE_CHART = 3
    }

    data class Balance(
        val balance: CharSequence,
        val fiat: CharSequence,
        val iconUri: Uri,
    ): Item(TYPE_BALANCE)

    data class Actions(
        val swapUri: Uri,
        val swap: Boolean,
        val send: Boolean,
        val walletAddress: String,
        val tokenAddress: String,
        val token: TokenEntity,
        val walletType: WalletType,
    ): Item(TYPE_ACTIONS)

    data class Price(
        val fiatPrice: CharSequence,
        val rateDiff24h: CharSequence
    ): Item(TYPE_PRICE)

    data class Chart(
        val data: List<ChartEntity>,
        val square: Boolean
    ): Item(TYPE_CHART)
}