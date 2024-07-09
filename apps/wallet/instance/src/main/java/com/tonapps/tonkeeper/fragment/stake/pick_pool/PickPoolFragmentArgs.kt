package com.tonapps.tonkeeper.fragment.stake.pick_pool

import android.os.Bundle
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import com.tonapps.wallet.data.core.WalletCurrency
import uikit.base.BaseArgs

data class PickPoolFragmentArgs(
    val service: StakingService,
    val pickedPool: StakingPool,
    val currency: WalletCurrency
) : BaseArgs() {

    companion object {
        private const val KEY_SERVICE = "KEY_SERVICE "
        private const val KEY_PICKED_POOL = "KEY_PICKED_POOL"
        private const val KEY_CURRENCY = "KEY_CURRENCY "
    }

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putParcelable(KEY_PICKED_POOL, pickedPool)
            putParcelable(KEY_SERVICE, service)
            putParcelable(KEY_CURRENCY, currency)
        }
    }

    constructor(bundle: Bundle) : this(
        pickedPool = bundle.getParcelable(KEY_PICKED_POOL)!!,
        service = bundle.getParcelable(KEY_SERVICE)!!,
        currency = bundle.getParcelable(KEY_CURRENCY)!!
    )
}