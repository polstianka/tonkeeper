package com.tonapps.tonkeeper.ui.screen.battery.recharge

import android.os.Bundle
import com.tonapps.extensions.getParcelableCompat
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import uikit.base.BaseArgs

data class RechargeArgs(
    val token: AccountTokenEntity?,
    val isGift: Boolean
) : BaseArgs() {

    private companion object {
        private const val ARG_TOKEN = "token"
        private const val ARG_IS_GIFT = "is_gift"
    }

    constructor(bundle: Bundle) : this(
        token = bundle.getParcelableCompat(ARG_TOKEN),
        isGift = bundle.getBoolean(ARG_IS_GIFT)
    )

    override fun toBundle(): Bundle {
        val bundle = Bundle()
        bundle.putParcelable(ARG_TOKEN, token)
        bundle.putBoolean(ARG_IS_GIFT, isGift)
        return bundle
    }
}