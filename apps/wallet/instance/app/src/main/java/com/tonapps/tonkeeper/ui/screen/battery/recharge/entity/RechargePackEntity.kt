package com.tonapps.tonkeeper.ui.screen.battery.recharge.entity

import com.tonapps.icu.Coins
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.wallet.api.entity.ConfigEntity
import com.tonapps.wallet.data.battery.BatteryMapper
import com.tonapps.wallet.data.battery.entity.RechargeMethodEntity
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.settings.BatteryTransaction
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import java.math.BigDecimal
import java.math.RoundingMode

data class RechargePackEntity(
    val type: RechargePackType,
    private val rechargeMethod: RechargeMethodEntity,
    private val fiatRate: Coins,
    private val token: AccountTokenEntity,
    private val config: ConfigEntity,
    private val willBePaidManually: Boolean,
    private val shouldMinusReservedAmount: Boolean,
    private val currency: WalletCurrency,
) {
    private val rate: BigDecimal
        get() = rechargeMethod.rate.toBigDecimal()

    private val meansFee: BigDecimal
        get() = config.batteryMeanFees.toBigDecimal()

    private val tonAmount: BigDecimal
        get() = getTonAmount(meansFee, type)

    private val amountInToken: BigDecimal
        get() = rechargeMethod.fromTon(tonAmount).value

    private val reservedAmount: BigDecimal
        get() = rechargeMethod.fromTon(config.batteryReservedAmount).value

    val formattedAmount: CharSequence
        get() = CurrencyFormatter.formatFiat(
            currency = rechargeMethod.symbol,
            value = amountInToken,
        )

    val formattedFiatAmount: CharSequence
        get() = CurrencyFormatter.formatFiat(
            currency = currency.code,
            value = amountInToken.multiply(fiatRate.value)
        )

    val charges: Int
        get() = amountInToken.minus(
            if (shouldMinusReservedAmount) {
                reservedAmount
            } else {
                BigDecimal.valueOf(0)
            }
        ).multiply(rate).divide(meansFee, 0, RoundingMode.HALF_UP).toInt()

    val batteryLevel: Float
        get() = when (type) {
            RechargePackType.LARGE -> 1f
            RechargePackType.MEDIUM -> 0.5f
            RechargePackType.SMALL -> 0.25f
        }

    val transactions: Map<BatteryTransaction, Int>
        get() = mapOf(
            BatteryTransaction.SWAP to charges / BatteryMapper.calculateChargesAmount(
                config.batteryMeanPriceSwap,
                config.batteryMeanFees
            ),
            BatteryTransaction.NFT to charges / BatteryMapper.calculateChargesAmount(
                config.batteryMeanPriceNft,
                config.batteryMeanFees
            ),
            BatteryTransaction.JETTON to charges / BatteryMapper.calculateChargesAmount(
                config.batteryMeanPriceJetton,
                config.batteryMeanFees
            )
        )

    private val isAvailableToBuy: Boolean
        get() = willBePaidManually || rechargeMethod.minBootstrapValue?.let { amountInToken >= it.toBigDecimal() } ?: false

    private val isEnoughBalance: Boolean
        get() = token.balance.value.value >= amountInToken

    val isEnabled: Boolean
        get() = isAvailableToBuy && isEnoughBalance


    companion object {

        fun getTonAmount(meansFee: BigDecimal, type: RechargePackType): BigDecimal {
            return when (type) {
                RechargePackType.LARGE -> meansFee.multiply(BigDecimal.valueOf(400))
                RechargePackType.MEDIUM -> meansFee.multiply(BigDecimal.valueOf(250))
                RechargePackType.SMALL -> meansFee.multiply(BigDecimal.valueOf(150))
            }
        }

        fun getTonAmount(meansFee: String, type: RechargePackType): BigDecimal {
            val meansFeeBigDecimal = BigDecimal(meansFee)
            return getTonAmount(meansFeeBigDecimal, type)
        }
    }

}
