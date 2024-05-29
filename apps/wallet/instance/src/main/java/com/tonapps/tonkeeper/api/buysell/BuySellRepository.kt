package com.tonapps.tonkeeper.api.buysell

import android.content.Context
import com.tonapps.network.Network
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.internal.Tonkeeper
import com.tonapps.tonkeeper.api.swap.StonfiSwapAsset
import com.tonapps.tonkeeper.api.withRetry
import com.tonapps.tonkeeper.core.fiat.models.FiatSuccessUrlPattern
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap


class BuySellRepository(
    context: Context
) {

    companion object {
        const val MERCURYO_MERCHANT = "mercuryo"
        const val MOONPAY_MERCHANT = "moonpay"
        const val TRANSAK_MERCHANT = "transak"
        const val DREAMWALKERS_MERCHANT = "dreamwalkers"
    }

    private var cached = ConcurrentHashMap<String, List<BuySellOperator>>()
    suspend fun getOperators(currency: String, type: TradeType): List<BuySellOperator> {
        val key = "$currency$type"
        if (cached[key]?.isNotEmpty() == true) {
            return cached[key] ?: emptyList()
        }

        val types = loadOperators(currency, type)
        if (types.isNotEmpty()) {
            cached[key] = types
        }

        return types
    }

    fun getTypes(type: TradeType): List<BuySellType> {
        val result = mutableListOf<BuySellType>()
        result.add(BuySellType(TYPE_CREDIT, App.instance.getString(Localization.credit_card), R.drawable.cards))
        if (type == TradeType.SELL) {
            result.add(BuySellType(TYPE_MIR, "${App.instance.getString(Localization.credit_card)} RUB", R.drawable.mir))
        }
        //result.add(BuySellType(TYPE_CRYPTO, "Cryptocurrency", R.drawable.crypto))
        result.add(BuySellType(TYPE_GPAY, App.instance.getString(Localization.google_pay), R.drawable.gpay))
        return result
    }

    private suspend fun loadOperators(currency: String, type: TradeType): List<BuySellOperator> {
        val result = mutableListOf<BuySellOperator>()
        val url = "https://boot.tonkeeper.com/widget/${type.type}/rates?currency=$currency"
        val response = withRetry { Network.get(url) } ?: return result
        try {
            val data = JSONObject(response).getJSONArray("items")
            for (i in 0 until data.length()) {
                val item = data.getJSONObject(i)
                result.add(BuySellOperator(item))
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        val response2 = withRetry { Tonkeeper.get("fiat/methods") }
        response2?.let { resp2 ->
            val json = resp2.getJSONObject("data")
            val category = json.getJSONArray(type.type)
            val itemsArray = category.getJSONObject(0).getJSONArray("items")
            for (i in 0 until itemsArray.length()) {
                val item = itemsArray.getJSONObject(i)
                val id = item.optString("id")
                try {
                    result.find { it.id == id }?.let { bs ->
                        item.optJSONArray("info_buttons")?.let {
                            if (it.length() == 2) {
                                bs.termsOfUseUrl = it.getJSONObject(0).optString("url")
                                bs.privacyPolicyUrl = it.getJSONObject(1).optString("url")
                            }
                        }
                        item.optJSONObject("action_button")?.let {
                            bs.actionUrl = it.optString("url")
                        }
                        bs.subtitle = item.optString("subtitle", "")
                        item.optJSONObject("successUrlPattern")?.let {
                            bs.successUrlPattern = FiatSuccessUrlPattern(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return result
    }
}