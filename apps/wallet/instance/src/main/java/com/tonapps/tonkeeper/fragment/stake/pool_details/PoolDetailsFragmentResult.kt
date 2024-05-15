package com.tonapps.tonkeeper.fragment.stake.pool_details

import android.os.Bundle
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import uikit.base.BaseArgs

data class PoolDetailsFragmentResult(
    val pickedPool: StakingPool
) : BaseArgs() {

    companion object {
        private const val KEY_PICKED_POOL = "KEY_PICKED_POOL "
    }
    override fun toBundle(): Bundle {
        return Bundle().apply {
            putParcelable(KEY_PICKED_POOL, pickedPool)
        }
    }

    constructor(bundle: Bundle) : this(pickedPool = bundle.getParcelable(KEY_PICKED_POOL)!!)
}