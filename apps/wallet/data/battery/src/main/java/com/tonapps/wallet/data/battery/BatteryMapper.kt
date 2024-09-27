package com.tonapps.wallet.data.battery

import com.tonapps.icu.Coins
import com.tonapps.wallet.data.battery.entity.RechargeMethodEntity
import java.math.BigDecimal
import java.math.RoundingMode

object BatteryMapper {

    fun convertToCharges(
        balance: Coins,
        meanFees: String
    ): Int {
        val meanFeesBigDecimal = BigDecimal(meanFees)
        return balance.value.divide(meanFeesBigDecimal, 0, RoundingMode.UP).toInt()
    }

    fun calculateChargesAmount(
        transactionCost: String,
        meanFees: String
    ): Int {
        val meanFeesBigDecimal = BigDecimal(meanFees)
        val transactionCostBigDecimal = BigDecimal(transactionCost)

        return transactionCostBigDecimal.divide(meanFeesBigDecimal, 0, RoundingMode.HALF_UP)
            .toInt()
    }

    fun calculateCryptoCharges(method: RechargeMethodEntity, meanFees: String, amount: Coins): Int {
        val meanFeesBigDecimal = BigDecimal(meanFees)
        val rateBigDecimal = BigDecimal(method.rate)
        return rateBigDecimal.divide(meanFeesBigDecimal, 20, RoundingMode.HALF_UP).multiply(amount.value).setScale(0, RoundingMode.FLOOR).toInt()
    }
}