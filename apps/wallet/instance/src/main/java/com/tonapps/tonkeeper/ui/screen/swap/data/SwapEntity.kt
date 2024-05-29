package com.tonapps.tonkeeper.ui.screen.swap.data

import android.os.Parcelable
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.core.scaleDownAndStripTrailingZeros
import com.tonapps.tonkeeper.core.toDisplayAmount
import com.tonapps.tonkeeper.ui.screen.swap.stonfi.Stonfi
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class SwapEntity(
    val token: TokenEntity? = null,
    val asset: AssetEntity? = null,
    val amount: SwapAmount = SwapAmount.EMPTY,
    val accountToken: AccountTokenEntity? = null,
    val minDecimalsInUserCurrency: Int = -1,
    val minDecimalsInUsd: Int = -1,
    val displayData: SwapEntityDisplayData? = SwapEntityDisplayData.valueOf(
        asset, accountToken, amount,
        minDecimalsInWtfIsKilometer = minDecimalsInUsd,
        minDecimalsInUserCurrency = minDecimalsInUserCurrency
    )
): Parcelable {
    companion object {
        val EMPTY = SwapEntity()

        fun valueOf(entity: TokenEntity?, amount: SwapAmount = SwapAmount.EMPTY): SwapEntity =
            entity?.let { SwapEntity(token = entity, amount = amount) } ?: EMPTY
    }
    @IgnoredOnParcel
    val hasToken: Boolean =
        token != null
    @IgnoredOnParcel
    val hasAmount: Boolean =
        !amount.isEmpty

    @IgnoredOnParcel
    val balance: BigDecimal =
        asset?.balance?.number ?: BigDecimal.ZERO

    fun withAmount(amount: SwapAmount): SwapEntity =
        if (this.amount == amount) {
            this
        } else if (this.amount.amount == amount.amount) {
            this.copy(
                amount = amount
            )
        } else {
            this.copy(
                amount = amount,
                displayData = SwapEntityDisplayData.valueOf(asset, accountToken, amount,
                    minDecimalsInWtfIsKilometer = minDecimalsInUsd,
                    minDecimalsInUserCurrency = minDecimalsInUserCurrency
                )
            )
        }

    fun withAmountOrigin(origin: SwapAmount.Origin): SwapEntity =
        withAmount(amount.withOrigin(origin))

    fun withRemoteDetails(asset: AssetEntity?, accountToken: AccountTokenEntity?): SwapEntity {
        return if (this.asset == asset) {
            this
        } else {
            copy(
                asset = asset,
                displayData = SwapEntityDisplayData.valueOf(asset, accountToken, amount,
                    minDecimalsInWtfIsKilometer = minDecimalsInUsd,
                    minDecimalsInUserCurrency = minDecimalsInUserCurrency
                )
            )
        }
    }

    @IgnoredOnParcel
    val isAmountInsufficient: Boolean =
        displayData != null && displayData.isAmountInsufficient

    @IgnoredOnParcel
    val isTon: Boolean =
        asset?.token?.isTon ?: token?.isTon ?: false

    @IgnoredOnParcel
    val tokenAddress: String? =
        asset?.token?.address ?: token?.address
}

@Parcelize
data class SwapEntityDisplayData(
    val balance: String,
    val balanceMinusAmount: String,
    val balanceMinusAmountNegative: Boolean,
    val amountInUsd: String,
    val amountInUserCurrency: String,
    val isAmountInsufficient: Boolean,
    val hasAcquiredFunds: Boolean
) : Parcelable {
    companion object {
        fun valueOf (asset: AssetEntity?, accountToken: AccountTokenEntity?, amount: SwapAmount,
                     minDecimalsInWtfIsKilometer: Int,
                     minDecimalsInUserCurrency: Int): SwapEntityDisplayData? {
            val balance = asset?.balance?.number ?: accountToken?.balance?.value?.toBigDecimal()?.stripTrailingZeros()
            val token = asset?.token ?: accountToken?.balance?.token

            if (balance == null) return null

            val decimals = token?.decimals ?: 2
            val isTon = token?.isTon ?: false
            val priceInUsd = asset?.anyPriceInUsd
            val priceInUserCurrency = asset?.priceInUserCurrency?.takeIf { asset.userCurrency != null }

            val format = App.defaultNumberFormat()

            val balanceMinusAmount = balance
                .minus(amount.amount.number)
                .scaleDownAndStripTrailingZeros(decimals)
            val extraFees = if (SwapConfig.EXCLUDE_GAS_FROM_INSUFFICIENT_FUNDS) {
                Stonfi.extraFees(isTon)
            } else {
                BigDecimal.ZERO
            }
            val isAmountInsufficient = !amount.isEmpty && balance < amount.amount.number + extraFees

            return SwapEntityDisplayData(
                balance = balance.toDisplayAmount(format, decimals),
                balanceMinusAmount = balanceMinusAmount.toDisplayAmount(format, decimals),
                balanceMinusAmountNegative = balanceMinusAmount < BigDecimal.ZERO,
                amountInUsd = priceInUsd?.let {
                    (it * amount.amount.number).toUsdString(minDecimalsInWtfIsKilometer)
                } ?: "",
                amountInUserCurrency = priceInUserCurrency?.let {
                    (it * amount.amount.number).toCurrencyString(asset.userCurrency!!, minDecimalsInUserCurrency)
                } ?: "",
                isAmountInsufficient = isAmountInsufficient,
                hasAcquiredFunds = balance > BigDecimal.ZERO
            )
        }

        fun SwapEntityDisplayData?.amountInMatchingCurrency(other: SwapEntityDisplayData?): String {
            return when {
                this == null -> ""
                other == null -> amountInAnyCurrency
                amountInUserCurrency.isNotEmpty() && other.amountInUserCurrency.isNotEmpty() -> {
                    amountInUserCurrency
                }
                amountInUsd.isNotEmpty() && other.amountInUsd.isNotEmpty() -> {
                    amountInUsd
                }
                else -> {
                    amountInAnyCurrency
                }
            }
        }
    }

    @IgnoredOnParcel
    private val amountInAnyCurrency: String =
        amountInUserCurrency.ifEmpty { amountInUsd }
}