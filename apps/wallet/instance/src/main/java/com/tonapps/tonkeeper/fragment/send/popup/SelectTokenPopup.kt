package com.tonapps.tonkeeper.fragment.send.popup

import android.content.Context
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.uikit.icon.UIKitIcon
import uikit.extensions.textWithLabel
import com.tonapps.tonkeeper.popup.ActionSheet
import com.tonapps.wallet.data.token.entities.AccountTokenEntity

class SelectTokenPopup(context: Context): ActionSheet(context) {

    var tokens = listOf<AccountTokenEntity>()
        set(value) {
            if (field != value) {
                field = value
                updateTokens()
            }
        }

    var selectedToken: AccountTokenEntity? = null
        set(value) {
            if (field != value) {
                field = value
                updateTokens()
            }
        }

    var doOnSelectJetton: ((AccountTokenEntity) -> Unit)? = null

    init {
        doOnItemClick = { item ->
            val jetton = tokens[item.id.toInt()]
            doOnSelectJetton?.invoke(jetton)
        }
    }

    private fun updateTokens() {
        clearItems()
        for ((index, token) in tokens.withIndex()) {
            val format = CurrencyFormatter.format(value = token.balance.value)
            val title = context.textWithLabel(token.symbol, format)
            val selected = token == selectedToken
            addItem(
                id = index.toLong(),
                title = title,
                icon = if (selected) {
                    getDrawable(UIKitIcon.ic_done_16)
                } else {
                    null
                },
                imageUri = token.imageUri
            )
        }
    }
}