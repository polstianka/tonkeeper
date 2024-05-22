package com.tonapps.tonkeeper.fragment.stake.balance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.fragment.stake.domain.GetStakingPoolLiquidJettonCase
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class StakedBalanceViewModel(
    getStakingPoolLiquidJettonCase: GetStakingPoolLiquidJettonCase
) : ViewModel() {

    private val _events = MutableSharedFlow<StakedBalanceEvent>()

    val events: Flow<StakedBalanceEvent>
        get() = _events
    val args = MutableSharedFlow<StakedBalanceArgs>(replay = 1)
    val jetton = args.map {
        getStakingPoolLiquidJettonCase.execute(
            it.stakedBalance.pool,
            it.stakedBalance.currency
        )
    }.shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    fun provideArgs(args: StakedBalanceArgs) {
        emit(this.args, args)
    }

    fun onCloseClicked() {
        emit(_events, StakedBalanceEvent.NavigateBack)
    }

    fun onStakeClicked() = viewModelScope.launch {
        val args = args.first()
        val event = StakedBalanceEvent.NavigateToStake(
            args.stakedBalance.pool,
            args.stakedBalance.service,
            StakingDirection.STAKE
        )
        _events.emit(event)
    }

    fun onUnstakeClicked() = viewModelScope.launch {
        val args = args.first()
        val event = StakedBalanceEvent.NavigateToStake(
            args.stakedBalance.pool,
            args.stakedBalance.service,
            StakingDirection.UNSTAKE
        )
        _events.emit(event)
    }
}