package com.tonapps.tonkeeper.fragment.stake.root

import com.tonapps.tonkeeper.fragment.stake.domain.StakingTransactionType
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import com.tonapps.wallet.data.core.WalletCurrency
import java.math.BigDecimal

sealed class StakeEvent {
    object NavigateBack : StakeEvent()
    object ShowInfo : StakeEvent()
    data class SetInputValue(val value: BigDecimal) : StakeEvent()
    data class PickStakingOption(
        val items: List<StakingService>,
        val picked: StakingPool,
        val currency: WalletCurrency
    ) : StakeEvent()
    data class NavigateToConfirmFragment(
        val pool: StakingPool,
        val amount: BigDecimal,
        val type: StakingTransactionType,
        val isSendAll: Boolean
    ) : StakeEvent()
}