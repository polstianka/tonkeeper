package com.tonapps.tonkeeper.ui.screen.swapnative.choose.list

import android.net.Uri
import androidx.annotation.StringRes
import com.tonapps.uikit.list.BaseListItem

sealed class Item(type: Int, val contractAddress: String? = null) : BaseListItem(type),
    com.tonapps.uikit.list.ListCell {

    companion object {
        const val TYPE_TOKEN = 0
        const val TYPE_SUGGESTED = 1
        const val TYPE_TITLE = 2
    }

    data class TokenType(
        val iconUri: Uri?,
        val address: String,
        val displayName: String,
        val symbol: String,
        val balance: Double,
        val FiatBalance: String,
        val rate: Float,
        val balanceFormat: CharSequence,
        val hiddenBalance: Boolean,

        //
        val selected: Boolean,
        override val position: com.tonapps.uikit.list.ListCell.Position
    ) : Item(TYPE_TOKEN, address)

    data class Suggested(
        val iconUri: Uri?,
        val address: String,
        val displayName: String,
        val symbol: String,
        val balance: Double,
        val FiatBalance: String,
        val rate: Float,
        val balanceFormat: CharSequence,
        val hiddenBalance: Boolean,

        //
        val selected: Boolean,
        override val position: com.tonapps.uikit.list.ListCell.Position
    ) : Item(TYPE_SUGGESTED, address)

    data class Title(
        @StringRes val title: Int,

        override val position: com.tonapps.uikit.list.ListCell.Position
    ) : Item(TYPE_TITLE, null)


}