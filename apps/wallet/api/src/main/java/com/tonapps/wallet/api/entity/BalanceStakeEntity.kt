package com.tonapps.wallet.api.entity

import android.os.Parcelable
import com.tonapps.blockchain.Coin
import com.tonapps.wallet.api.entity.pool.PoolEntity
import com.tonapps.wallet.api.entity.pool.PoolStakeEntity
import io.tonapi.models.PoolImplementationType
import kotlinx.parcelize.Parcelize
import java.math.BigInteger

@Parcelize
data class BalanceStakeEntity(
    val pool: PoolEntity,
    val amountNano: BigInteger,
    val pendingDepositNano: BigInteger,
    val pendingWithdrawNano: BigInteger,
    val readyWithdrawNano: BigInteger,
    val tonRate: Float
): Parcelable {
    constructor(info: PoolStakeEntity, pool: PoolEntity, tonRate: Float) : this(
        pool = pool,
        amountNano = getAmountNano(info, pool),
        pendingDepositNano = info.pendingDepositNano,
        pendingWithdrawNano = info.pendingWithdrawNano,
        readyWithdrawNano = info.readyWithdrawNano,
        tonRate = tonRate
    )

    val totalAmountNano: BigInteger get() = amountNano + pendingDepositNano + pendingWithdrawNano + readyWithdrawNano

    val value: Float get() = Coin.toCoins(amountNano, TokenEntity.TON.decimals)

    val totalValue: Float get() = Coin.toCoins(totalAmountNano, TokenEntity.TON.decimals)

    companion object {
        fun getAmountNano(info: PoolStakeEntity, pool: PoolEntity): BigInteger {
            if (pool.implementation.type != PoolImplementationType.liquidTF) {
                val amount = info.amountNano - info.pendingWithdrawNano
                if (amount >= BigInteger.ZERO) {
                    return amount
                }
                return BigInteger.ZERO
            }

            return info.amountNano
        }
    }
}