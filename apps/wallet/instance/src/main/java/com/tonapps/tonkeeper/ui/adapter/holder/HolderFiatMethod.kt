package com.tonapps.tonkeeper.ui.adapter.holder

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.tonkeeper.ui.adapter.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.BaseListHolder
import com.tonapps.uikit.list.ListCell
import uikit.extensions.drawable
import uikit.widget.RadioButtonView

class HolderFiatMethod(
    parent: ViewGroup
): BaseListHolder<Item.FiatMethod>(parent, R.layout.holder_fiat_method) {

    private val iconView = findViewById<SimpleDraweeView>(R.id.icon)
    private val titleView = findViewById<AppCompatTextView>(R.id.title)
    private val subtitleView = findViewById<AppCompatTextView>(R.id.subtitle)
    private val radioView = findViewById<RadioButtonView>(R.id.radio)

    private var prevPosition: ListCell.Position? = null

    override fun onBind(item: Item.FiatMethod) {
        if (item.position != prevPosition) {
            itemView.background = item.position.drawable(itemView.context)
            prevPosition = item.position
        }

        item.onClickListener?.let { listener ->
            itemView.setOnClickListener {listener.invoke() }
        } ?: run {
            itemView.setOnClickListener(null)
        }

        iconView.setImageURI(item.iconUri)
        titleView.text = item.title
        subtitleView.text = item.subtitle
        radioView.checked = item.checked
    }
}