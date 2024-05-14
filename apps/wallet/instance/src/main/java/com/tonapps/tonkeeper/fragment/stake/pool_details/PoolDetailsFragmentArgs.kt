package com.tonapps.tonkeeper.fragment.stake.pool_details

import android.os.Bundle
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import uikit.base.BaseArgs

data class PoolDetailsFragmentArgs(
    val service: StakingService,
    val pool: StakingPool
) : BaseArgs() {

    companion object {
        private const val KEY_SERVICE = "KEY_SERVICE"
        private const val KEY_POOL = "KEY_POOL"
    }

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putParcelable(KEY_SERVICE, service)
            putParcelable(KEY_POOL, pool)
        }
    }

    constructor(bundle: Bundle) : this(
        service = bundle.getParcelable(KEY_SERVICE)!!,
        pool = bundle.getParcelable(KEY_POOL)!!
    )
}