package com.tonapps.tonkeeper.fragment.stake.balance

sealed class StakedBalanceEvent {
    object NavigateBack : StakedBalanceEvent()
}