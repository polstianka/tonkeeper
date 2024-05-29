package com.tonapps.tonkeeper.api.buysell

import androidx.annotation.DrawableRes
import com.tonapps.tonkeeper.core.fiat.models.FiatSuccessUrlPattern
import com.tonapps.uikit.list.BaseListItem
import org.json.JSONObject

const val TYPE_CREDIT = 0
const val TYPE_MIR = 1
const val TYPE_CRYPTO = 2
const val TYPE_GPAY = 3

data class BuySellType(
    val id: Int,
    val title: String,
    @DrawableRes
    val iconRes: Int,
    val selected: Boolean = false
): BaseListItem()

data class BuySellOperator(
    val id: String,
    val name: String,
    val logo: String,
    val currency: String,
    val rate: Float,
    val minTonBuyAmount: Long,
    val minTonSellAmount: Long,
    val selected: Boolean = false,
    val best: Boolean = false,
    var actionUrl: String = "",
    var termsOfUseUrl: String = "",
    var privacyPolicyUrl: String = "",
    var successUrlPattern: FiatSuccessUrlPattern? = null,
    var subtitle: String = ""
): BaseListItem() {
    constructor(json: JSONObject) : this(
        json.optString("id", ""),
        json.optString("name", ""),
        json.optString("logo", ""),
        json.optString("currency", ""),
        json.optDouble("rate", 0.0).toFloat(),
        json.optLong("min_ton_buy_amount", 0),
        json.optLong("min_ton_sell_amount", 0)
    )
}

sealed class TradeType {

    abstract val type: String

    data object BUY: TradeType() {
        override val type: String
            get() = "buy"
    }

    data object SELL : TradeType() {
        override val type: String
            get() = "sell"
    }
}