package com.tonapps.tonkeeper.fragment.stake.unstake

import android.os.Bundle
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakedBalance
import uikit.base.BaseArgs

data class UnstakeArgs(val balance: StakedBalance) : BaseArgs() {
    companion object {
        private const val KEY_BALANCE = "KEY_BALANCE "
    }

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putParcelable(KEY_BALANCE, balance)
        }
    }

    constructor(bundle: Bundle) : this(
        balance = bundle.getParcelable(KEY_BALANCE)!!
    )
}
