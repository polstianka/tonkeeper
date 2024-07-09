package com.tonapps.tonkeeper.fragment.stake.unstake

import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import java.math.BigDecimal

sealed class UnstakeEvent {

    object NavigateBack : UnstakeEvent()
    data class FillInput(val text: String) : UnstakeEvent()
    data class NavigateToConfirmStake(
        val pool: StakingPool,
        val amount: BigDecimal,
        val isSendAll: Boolean
    ) : UnstakeEvent()
}