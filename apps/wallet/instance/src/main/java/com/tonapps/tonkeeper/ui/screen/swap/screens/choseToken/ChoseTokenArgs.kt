package com.tonapps.tonkeeper.ui.screen.swap.screens.choseToken

import android.os.Bundle
import android.os.Parcelable
import com.tonapps.extensions.getParcelableCompat
import com.tonapps.extensions.toByteArray
import com.tonapps.tonkeeper.ui.screen.swap.SwapArgs
import com.tonapps.tonkeeper.ui.screen.swap.model.assets.Asset
import uikit.base.BaseArgs

data class ChoseTokenArgs(
    val listItemToken: List<Asset>,
    val selectedToken: Asset?,
    val type: Boolean
) : BaseArgs() {


    // if type true is send type, also if type false is receive type

    private companion object {
        private const val ARG_LIST_ITEM_TOKEN = "list_item_token"
        private const val ARG_OTHER_SELECTED_TOKEN = "other_selected_token"
        private const val ARG_TYPE_TOKEN = "token_type"
    }
    constructor(bundle: Bundle) : this(
        listItemToken = bundle.getParcelableCompat(ARG_LIST_ITEM_TOKEN)!!,
        selectedToken = bundle.getParcelableCompat(ARG_OTHER_SELECTED_TOKEN),
        type = bundle.getBoolean(ARG_TYPE_TOKEN)
    )


    override fun toBundle(): Bundle = Bundle().apply {
        putParcelableArrayList(ARG_LIST_ITEM_TOKEN, ArrayList<Parcelable>(listItemToken))
        selectedToken?.let {
            putParcelable(ARG_OTHER_SELECTED_TOKEN, selectedToken )
        }
        putBoolean(ARG_TYPE_TOKEN, type)
    }

}