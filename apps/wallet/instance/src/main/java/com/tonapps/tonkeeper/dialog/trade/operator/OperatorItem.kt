package com.tonapps.tonkeeper.dialog.trade.operator

import android.os.Parcelable
import com.tonapps.tonkeeper.core.fiat.models.FiatSuccessUrlPattern
import kotlinx.parcelize.Parcelize

@Parcelize
data class OperatorItem(
    val id: String,
    val title: String,
    val paymentUrl: String,
    val iconUrl: String,
    val subtitle: String,
    val successUrlPattern: FiatSuccessUrlPattern?,
    val rate: Double?,
    val fiatCurrency: String,
    val isSelected: Boolean,
    val minTonBuyAmount: Double,
    val minTonSellAmount: Double,
) : Parcelable
