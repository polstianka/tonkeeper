package com.tonapps.tonkeeper.fragment.stake.confirm

import com.tonapps.tonkeeper.fragment.stake.domain.StakingTransactionType

sealed class ConfirmStakeEvent {
    object NavigateBack : ConfirmStakeEvent()
    data class CloseFlow(
        val type: StakingTransactionType,
        val navigateToHistory: Boolean
    ) : ConfirmStakeEvent()
    object RestartSlider : ConfirmStakeEvent()
}