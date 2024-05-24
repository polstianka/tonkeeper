package com.tonapps.tonkeeper.fragment.stake.root

import android.os.Bundle
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import uikit.base.BaseArgs

data class StakeArgs(
    val pool: StakingPool?,
    val service: StakingService?
) : BaseArgs() {
    companion object {
        private const val KEY_POOL = "KEY_POOL "
        private const val KEY_SERVICE = "KEY_SERVICE "
    }

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putParcelable(KEY_POOL, pool)
            putParcelable(KEY_SERVICE, service)
        }
    }

    constructor(bundle: Bundle) : this(
        pool = bundle.getParcelable(KEY_POOL),
        service = bundle.getParcelable(KEY_SERVICE)
    )
}
