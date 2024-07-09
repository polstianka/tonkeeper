package com.tonapps.tonkeeper.fragment.stake.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.presentation.formatApy
import com.tonapps.tonkeeper.fragment.stake.presentation.minStakingText
import com.tonapps.tonkeeperx.R
import uikit.widget.ChipView
import uikit.widget.ColumnLayout

class PoolDetailsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ColumnLayout(context, attrs, defStyle) {

    private val chip: ChipView?
        get() = findViewById(R.id.view_pool_details_chip)
    private val apy: TextView?
        get() = findViewById(R.id.view_pool_details_apy)
    private val minDeposit: TextView?
        get() = findViewById(R.id.view_pool_details_min_deposit)

    init {
        inflate(context, R.layout.view_pool_details, this)
    }

    fun setPool(pool: StakingPool) {
        chip?.isVisible = pool.isMaxApy
        apy?.text = pool.formatApy()
        minDeposit?.text = pool.minStakingText()
    }
}