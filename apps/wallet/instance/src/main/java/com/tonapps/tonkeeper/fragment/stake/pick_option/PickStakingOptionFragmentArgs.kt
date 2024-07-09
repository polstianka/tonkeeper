package com.tonapps.tonkeeper.fragment.stake.pick_option

import android.os.Bundle
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import com.tonapps.wallet.data.core.WalletCurrency
import uikit.base.BaseArgs
import java.util.ArrayList

data class PickStakingOptionFragmentArgs(
    val options: List<StakingService>,
    val picked: StakingPool,
    val currency: WalletCurrency
) : BaseArgs() {

    companion object {
        const val KEY_OPTIONS = "KEY_OPTIONS "
        const val KEY_PICKED = "KEY_PICKED "
        const val KEY_CURRENCY = "KEY_CURRENCY "
    }

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putParcelableArrayList(KEY_OPTIONS, ArrayList(options))
            putParcelable(KEY_PICKED, picked)
            putParcelable(KEY_CURRENCY, currency)
        }
    }

    constructor(bundle: Bundle) : this(
        options = bundle.getParcelableArrayList(KEY_OPTIONS)!!,
        picked = bundle.getParcelable(KEY_PICKED)!!,
        currency = bundle.getParcelable(KEY_CURRENCY)!!
    )
}
