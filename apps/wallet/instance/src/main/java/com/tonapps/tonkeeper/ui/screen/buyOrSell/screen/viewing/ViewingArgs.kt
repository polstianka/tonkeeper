package com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.viewing

import android.os.Bundle
import uikit.base.BaseArgs

data class ViewingArgs(
    val urlLoad: String,
    val upBarTitle: String
) : BaseArgs() {

    private companion object {
        private const val ARGS_KEY_URL_LOAD = "KEY_URL_LOAD"
        private const val ARGS_KEY_UP_BAR_TITLE = "KEY_UP_BAR_TITLE"
    }

    constructor(bundle: Bundle) : this(
        urlLoad = bundle.getString(ARGS_KEY_URL_LOAD).toString(),
        upBarTitle = bundle.getString(ARGS_KEY_UP_BAR_TITLE).toString()
    )

    override fun toBundle(): Bundle = Bundle().apply {
        putString(ARGS_KEY_URL_LOAD, urlLoad)
        putString(ARGS_KEY_UP_BAR_TITLE, upBarTitle)
    }

}