package com.tonapps.tonkeeper.fragment.stake.confirm

import android.os.Bundle
import com.tonapps.extensions.getEnum
import com.tonapps.extensions.putEnum
import com.tonapps.tonkeeper.fragment.stake.domain.StakingTransactionType
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import uikit.base.BaseArgs
import java.math.BigDecimal

data class ConfirmStakeArgs(
    val pool: StakingPool,
    val amount: BigDecimal,
    val type: StakingTransactionType
) : BaseArgs() {
    companion object {
        private const val KEY_POOL = "KEY_POOL "
        private const val KEY_AMOUNT = "KEY_AMOUNT"
        private const val KEY_TYPE = "KEY_TYPE "
    }
    override fun toBundle(): Bundle {
        return Bundle().apply {
            putParcelable(KEY_POOL, pool)
            putSerializable(KEY_AMOUNT, amount)
            putEnum(KEY_TYPE, type)
        }
    }

    constructor(bundle: Bundle) : this(
        pool = bundle.getParcelable(KEY_POOL)!!,
        amount = bundle.getSerializable(KEY_AMOUNT) as BigDecimal,
        type = bundle.getEnum(KEY_TYPE, StakingTransactionType.DEPOSIT)
    )
}