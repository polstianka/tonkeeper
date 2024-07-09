package com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.currencylist

import android.os.Bundle
import android.os.Parcelable
import com.tonapps.extensions.getParcelableCompat
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel.LayoutByCountry
import uikit.base.BaseArgs

data class CurrencyListArgs (
    val itemList: List<LayoutByCountry>
) : BaseArgs() {
    private companion object {
        private const val ARGS_KEY_LIST = "KEY_LIST"
    }
    constructor(bundle: Bundle) : this(
        itemList = bundle.getParcelableCompat(ARGS_KEY_LIST)!!,
    )
    override fun toBundle(): Bundle = Bundle().apply {
        putParcelableArrayList(ARGS_KEY_LIST, ArrayList<Parcelable>(itemList))
    }
}