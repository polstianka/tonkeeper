package com.tonapps.tonkeeper.fragment.stake.pick_pool

import android.os.Bundle
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import uikit.base.BaseArgs
import java.util.ArrayList

data class PickPoolFragmentArgs(
    val title: String,
    val pools: List<StakingPool>,
    val pickedPool: StakingPool
) : BaseArgs() {

    companion object {
        private const val KEY_TITLE = "KEY_TITLE"
        private const val KEY_POOLS = "KEY_POOLS "
        private const val KEY_PICKED_POOL = "KEY_PICKED_POOL"
    }

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putParcelableArrayList(KEY_POOLS, ArrayList(pools))
            putParcelable(KEY_PICKED_POOL, pickedPool)
            putString(KEY_TITLE, title)
        }
    }

    constructor(bundle: Bundle) : this(
        title = bundle.getString(KEY_TITLE)!!,
        pools = bundle.getParcelableArrayList(KEY_POOLS)!!,
        pickedPool = bundle.getParcelable(KEY_PICKED_POOL)!!
    )
}