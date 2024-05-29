package com.tonapps.tonkeeper.ui.screen.swapnative.choose.list

import android.net.Uri

data class TokenTypeItem(
    val iconUri: Uri?,
    val address: String,
    val displayName: String,
    val symbol: String,
    val balance: Double,
    val FiatBalance: String,
    val rate : Float,
    val balanceFormat: CharSequence,
    val hiddenBalance: Boolean,

    //
    val selected: Boolean,
    override val position: com.tonapps.uikit.list.ListCell.Position
) : com.tonapps.uikit.list.BaseListItem(), com.tonapps.uikit.list.ListCell {

}