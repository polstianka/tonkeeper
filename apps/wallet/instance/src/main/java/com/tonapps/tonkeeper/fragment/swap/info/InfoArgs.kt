package com.tonapps.tonkeeper.fragment.swap.info

import android.content.Context
import android.os.Bundle
import com.tonapps.wallet.localization.Localization
import uikit.base.BaseArgs

data class InfoArgs(
    val title: String,
    val text: String
) : BaseArgs() {

    companion object {
        private const val KEY_TITLE = "KEY_TITLE "
        private const val KEY_TEXT = "KEY_TEXT "

        fun priceImpact(context: Context): InfoArgs {
            return InfoArgs(
                title = context.getString(Localization.price_impact),
                text = context.getString(Localization.price_impact_info)
            )
        }

        fun minReceived(context: Context): InfoArgs {
            return InfoArgs(
                title = context.getString(Localization.minimum_received),
                text = context.getString(Localization.min_received_info)
            )
        }

        fun liquidityProviderFee(context: Context): InfoArgs {
            return InfoArgs(
                title = context.getString(Localization.liquidity_provider_fee),
                text = context.getString(Localization.liquidity_provider_fee_info)
            )
        }
    }

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putString(KEY_TITLE, title)
            putString(KEY_TEXT, text)
        }
    }

    constructor(bundle: Bundle) : this(
        title = bundle.getString(KEY_TITLE)!!,
        text = bundle.getString(KEY_TEXT)!!
    )
}