package com.tonapps.tonkeeper.ui.screen.battery.recharge.list.holder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.extensions.clipboardText
import com.tonapps.tonkeeper.ui.screen.battery.recharge.list.Item
import com.tonapps.tonkeeperx.R
import uikit.widget.InputView
import uikit.widget.RowLayout

class AddressHolder(
    parent: ViewGroup,
    private val onTextChange: (String) -> Unit,
    private val openAddressBook: () -> Unit,
) : Holder<Item.Address>(parent, R.layout.fragment_recharge_address) {

    private val inputView = itemView.findViewById<InputView>(R.id.address)
    private val addressActionsView = itemView.findViewById<RowLayout>(R.id.address_actions)
    private val pasteView = itemView.findViewById<AppCompatTextView>(R.id.paste)
    private val addressBookView = itemView.findViewById<AppCompatImageView>(R.id.address_book)

    override fun onBind(item: Item.Address) {
        inputView.doOnTextChange = { text ->
            onTextChange(text)
            inputView.loading = text.isNotBlank()
            addressActionsView.visibility = if (text.isBlank()) View.VISIBLE else View.GONE
        }
        inputView.loading = item.loading
        inputView.error = item.error

        addressBookView.setOnClickListener {
            openAddressBook()
        }

        pasteView.setOnClickListener {
            inputView.text = context.clipboardText()
        }
    }
}