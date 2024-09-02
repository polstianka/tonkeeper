package com.tonapps.tonkeeper.ui.screen.battery.refill.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.extensions.clipboardText
import com.tonapps.tonkeeper.ui.screen.battery.refill.list.Item
import com.tonapps.tonkeeperx.R
import uikit.widget.InputView
import uikit.widget.RowLayout

class PromoHolder(
    parent: ViewGroup,
    private val onSubmitPromo: (String) -> Unit,
): Holder<Item.Promo>(parent, R.layout.fragment_battery_promo) {

    private val inputView = itemView.findViewById<InputView>(R.id.promo_input)
    private val actionsView = itemView.findViewById<RowLayout>(R.id.actions)
    private val pasteView = itemView.findViewById<AppCompatTextView>(R.id.paste)

    override fun onBind(item: Item.Promo) {
        inputView.singleLine = true
        inputView.doOnTextChange = { text ->
            inputView.error = false
            inputView.success = item.appliedPromo.isNotBlank() && text == item.appliedPromo
            actionsView.visibility = if (text.isBlank()) View.VISIBLE else View.GONE
        }
        inputView.loading = item.isLoading
        inputView.error = item.isError
        inputView.success = item.appliedPromo.isNotBlank()

        if (item.initialPromo != null) {
            inputView.text = item.initialPromo!!
        }

        if (inputView.isEmpty && !inputView.isFocused) {
            inputView.text = item.appliedPromo
        }

        inputView.setOnDoneActionListener {
            inputView.hideKeyboard()
            if (inputView.text.isNotBlank() && inputView.text != item.appliedPromo && !item.isLoading) {
                onSubmitPromo(inputView.text)
            }
        }

        pasteView.setOnClickListener {
            inputView.text = context.clipboardText()
            inputView.hideKeyboard()
            onSubmitPromo(inputView.text)
        }
    }
}