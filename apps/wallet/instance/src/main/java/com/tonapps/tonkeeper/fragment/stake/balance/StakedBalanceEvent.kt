package com.tonapps.tonkeeper.fragment.stake.balance

import com.tonapps.tonkeeper.fragment.stake.domain.StakingTransactionType
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakedBalance
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset

sealed class StakedBalanceEvent {
    object NavigateBack : StakedBalanceEvent()
    data class NavigateToStake(
        val balance: StakedBalance,
        val stakingDirection: StakingTransactionType
    ) : StakedBalanceEvent()
    data class NavigateToLink(val url: String) : StakedBalanceEvent()
    data class NavigateToToken(val asset: DexAsset): StakedBalanceEvent()
}