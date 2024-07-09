package com.tonapps.tonkeeper.fragment.swap.pick_asset.rv

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import coil.transform.RoundedCornersTransformation
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.loadUri
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.accentBlueColor
import com.tonapps.uikit.color.accentGreenColor
import com.tonapps.uikit.color.resolveColor
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.wallet.localization.Localization
import uikit.extensions.dp
import uikit.extensions.setThrottleClickListener
import uikit.widget.ChipView
import uikit.widget.item.BaseItemView
import java.math.BigDecimal

class TokenHolder(
    parent: ViewGroup,
    val onItemClicked: (TokenListItem) -> Unit
) : BaseListHolder<TokenListItem>(parent, R.layout.view_token_item) {

    private val baseItemView: BaseItemView = itemView as BaseItemView
    private val icon: ImageView = findViewById(R.id.view_token_item_icon)
    private val symbol: TextView = findViewById(R.id.view_token_item_symbol)
    private val amountCrypto: TextView = findViewById(R.id.view_token_item_amount_crypto)
    private val amountFiat: TextView = findViewById(R.id.view_token_item_amount_fiat)
    private val name: TextView = findViewById(R.id.view_token_item_name)
    private val chip: ChipView = findViewById(R.id.view_token_item_chip)

    override fun onBind(item: TokenListItem) {
        baseItemView.position = item.position
        icon.loadUri(item.iconUri, RoundedCornersTransformation(22f.dp))
        symbol.text = item.symbol
        name.text = item.name
        baseItemView.setThrottleClickListener { onItemClicked(item) }

        amountCrypto.text = CurrencyFormatter.format("", item.model.balance)
        amountCrypto.setTextColor(getCryptoBalanceTextColor(item))
        val fiatText = if (item.model.balance.compareTo(BigDecimal.ZERO) == 0) {
            ""
        } else {
            val fiatAmount = item.model.balance * item.model.rate.rate
            CurrencyFormatter.format(item.model.rate.currency.code, fiatAmount)
        }
        amountFiat.text = fiatText
        when (item.itemType) {
            TokenItemType.TO_SEND -> {
                chip.isVisible = true
                chip.text = getString(Localization.to_send)
                chip.color = context.accentBlueColor
            }
            TokenItemType.TO_RECEIVE -> {
               chip.isVisible = true
                chip.text = getString(Localization.to_receive)
                chip.color = context.accentGreenColor
            }
            TokenItemType.NORMAL -> {
                chip.isVisible = false
            }
        }
    }

    @ColorInt
    private fun getCryptoBalanceTextColor(item: TokenListItem): Int {
        val attr = when {
            item.model.balance == BigDecimal.ZERO ->
                com.tonapps.uikit.color.R.attr.textTertiaryColor
            else ->
                com.tonapps.uikit.color.R.attr.textPrimaryColor
        }
        return context.resolveColor(attr)
    }
}