package com.tonapps.tonkeeper.fragment.trade.pick_operator

import android.os.Bundle
import uikit.base.BaseArgs

class PickOperatorFragmentArgs(
    val id: String,
    val name: String,
    val country: String,
    val requestCode: Int
) : BaseArgs() {

    companion object {
        private const val KEY_ID = "KEY_ID"
        private const val KEY_NAME = "KEY_NAME"
        private const val KEY_COUNTRY = "KEY_COUNTRY"
        private const val KEY_REQUEST_CODE = "KEY_REQUEST_CODE"
    }

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putString(KEY_ID, id)
            putString(KEY_NAME, name)
            putString(KEY_COUNTRY, country)
            putInt(KEY_REQUEST_CODE, requestCode)
        }
    }

    constructor(bundle: Bundle) : this(
        id = bundle.getString(KEY_ID)!!,
        name = bundle.getString(KEY_NAME)!!,
        country = bundle.getString(KEY_COUNTRY)!!,
        requestCode = bundle.getInt(KEY_REQUEST_CODE)
    )
}