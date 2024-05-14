package com.tonapps.tonkeeper.fragment.stake.pool_details

import androidx.lifecycle.ViewModel
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.fragment.stake.presentation.formatApy
import com.tonapps.tonkeeper.fragment.stake.presentation.minStakingText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal

class PoolDetailsViewModel : ViewModel() {

    private val _events = MutableSharedFlow<PoolDetailsEvent>()
    private val args = MutableSharedFlow<PoolDetailsFragmentArgs>(replay = 1)

    val events: Flow<PoolDetailsEvent>
        get() = _events
    val title = args.map { it.pool.name }
    val apy = args.map { "${it.pool.formatApy()}%" }
    val isMaxApyVisible = args.map { it.pool.isMaxApy }
    val minimalDeposit = args.map { args ->
        args.pool.minStakingText()
    }
    fun provideArgs(args: PoolDetailsFragmentArgs) {
        emit(this.args, args)
    }

    fun onCloseClicked() {
        emit(_events, PoolDetailsEvent.FinishFlow)
    }

    fun onChevronClicked() {
        emit(_events, PoolDetailsEvent.NavigateBack)
    }
}