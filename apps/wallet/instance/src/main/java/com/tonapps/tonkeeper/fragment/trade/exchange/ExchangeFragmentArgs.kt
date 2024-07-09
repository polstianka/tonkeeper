package com.tonapps.tonkeeper.fragment.trade.exchange

import android.os.Bundle
import com.tonapps.extensions.getEnum
import com.tonapps.extensions.putEnum
import com.tonapps.tonkeeper.fragment.trade.domain.model.ExchangeDirection
import uikit.base.BaseArgs

data class ExchangeFragmentArgs(val direction: ExchangeDirection) : BaseArgs() {

    companion object {
        private const val KEY_DIRECTION = "KEY_DIRECTION "
    }
    override fun toBundle(): Bundle {
        return Bundle().apply {
            putEnum(KEY_DIRECTION, direction)
        }
    }

    constructor(bundle: Bundle) : this(bundle.getEnum(KEY_DIRECTION, ExchangeDirection.BUY))
}