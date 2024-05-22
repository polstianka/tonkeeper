package com.tonapps.tonkeeper.fragment.stake.balance

import android.os.Bundle
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakedBalance
import uikit.base.BaseArgs

data class StakedBalanceArgs(
    val stakedBalance: StakedBalance
) : BaseArgs() {
    companion object {
        private const val KEY_STAKED_BALANCE = "KEY_STAKED_BALANCE "
    }

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putParcelable(KEY_STAKED_BALANCE, stakedBalance)
        }
    }

    constructor(bundle: Bundle) : this(
        stakedBalance = bundle.getParcelable(KEY_STAKED_BALANCE)!!
    )
}
