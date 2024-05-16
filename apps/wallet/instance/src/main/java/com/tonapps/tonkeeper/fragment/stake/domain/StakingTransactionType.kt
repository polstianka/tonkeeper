package com.tonapps.tonkeeper.fragment.stake.domain

import com.tonapps.wallet.localization.R as LocalizationR

enum class StakingTransactionType {
    DEPOSIT,
    UNSTAKE;
}

fun StakingTransactionType.getOperationStringResId(): Int {
    return when (this) {
        StakingTransactionType.DEPOSIT -> LocalizationR.string.stake
        StakingTransactionType.UNSTAKE -> LocalizationR.string.unstake
    }
}