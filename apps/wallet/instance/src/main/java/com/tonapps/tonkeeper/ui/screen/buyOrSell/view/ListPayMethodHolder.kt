package com.tonapps.tonkeeper.ui.screen.buyOrSell.view

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeper.ui.screen.wallet.list.holder.Holder
import com.tonapps.tonkeeperx.R
import uikit.extensions.drawable

class ListPayMethodHolder(
    parent: ViewGroup,
    private val getActiveIndex: () -> Int,
    private val changeIndexHandler: (newIndex: Int) -> Unit,
) : Holder<Item.ListPayMethod>(parent, R.layout.list_pay_method_holder) {


    private val txName = findViewById<AppCompatTextView>(R.id.txName)
    private val imgMethod = findViewById<ImageView>(R.id.imgMethod)
    private val stateSelected = findViewById<RadioButton>(R.id.stateSelected)
    override fun onBind(item: Item.ListPayMethod) {
        itemView.background = item.position.drawable(context)
        txName.text = item.name
        imgMethod.setImageDrawable(item.image_url)
        val state = getActiveIndex() == bindingAdapterPosition
        stateSelected.isChecked = state
        itemView.setOnClickListener {
            changeIndexHandler(bindingAdapterPosition)
        }
    }
}