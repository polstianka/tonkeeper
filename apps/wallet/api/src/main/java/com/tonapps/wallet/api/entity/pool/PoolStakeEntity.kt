package com.tonapps.wallet.api.entity.pool

import android.os.Parcelable
import io.tonapi.models.AccountStakingInfo
import kotlinx.parcelize.Parcelize
import java.math.BigInteger

@Parcelize
data class PoolStakeEntity(
    val pool: String,
    val amountNano: BigInteger,
    val pendingDepositNano: BigInteger,
    val pendingWithdrawNano: BigInteger,
    val readyWithdrawNano: BigInteger
): Parcelable {
    constructor(info: AccountStakingInfo) : this(
        pool = info.pool,
        amountNano = info.amount.toBigInteger(),
        pendingDepositNano = info.pendingDeposit.toBigInteger(),
        pendingWithdrawNano = info.pendingWithdraw.toBigInteger(),
        readyWithdrawNano = info.readyWithdraw.toBigInteger()
    )

    val totalAmountNano: BigInteger get() = amountNano + pendingDepositNano + pendingWithdrawNano + readyWithdrawNano
}
