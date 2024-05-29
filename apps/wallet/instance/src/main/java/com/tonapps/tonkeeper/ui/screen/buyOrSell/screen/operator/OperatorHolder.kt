package com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.operator

import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel.LayoutByCountry
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeper.ui.screen.wallet.list.holder.Holder
import com.tonapps.tonkeeperx.R
import uikit.extensions.drawable
import uikit.widget.FrescoView

class OperatorHolder(
    parent: ViewGroup,
    private val getActiveIndex: () -> Int,
    private val onChangeIndex: ((item: Item.OperatorModel) -> Unit),
    private val changeIndexHandler: (newIndex: Int) -> Unit,
) :
    Holder<Item.OperatorModel>(parent, R.layout.operator_holder) {

    private val imgSelected = findViewById<FrescoView>(R.id.imgCurrency)
    private val txMarket = findViewById<TextView>(R.id.txMarket)
    private val txPriceResult = findViewById<TextView>(R.id.txPriceResult)
    private val stateSelected = findViewById<RadioButton>(R.id.stateSelected)

    override fun onBind(item: Item.OperatorModel) {
        Log.d("CurrencyListHolder", "item - $item" )
        itemView.background = item.position.drawable(context)
        imgSelected.setImageURI(item.logo.toUri(), this)
        txMarket.text = item.name
        txPriceResult.text = item.priceResult
        // Обработка клика
        itemView.setOnClickListener {
            changeIndexHandler(bindingAdapterPosition)
        }

        // Проверка и установка состояния RadioButton
        val state = getActiveIndex() == bindingAdapterPosition
        if(state) {
            onChangeIndex(item)
        }
        stateSelected.isChecked = state
    }

}