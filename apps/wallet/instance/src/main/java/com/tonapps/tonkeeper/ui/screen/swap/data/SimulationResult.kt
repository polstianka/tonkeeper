package com.tonapps.tonkeeper.ui.screen.swap.data

import android.os.Parcelable
import com.tonapps.tonkeeper.core.scaleDownAndStripTrailingZeros
import com.tonapps.tonkeeper.core.toDisplayAmount
import com.tonapps.wallet.api.entity.TokenEntity
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

@Parcelize
data class SimulationResult (
    val send: SwapEntity,
    val receive: SwapEntity,
    val isReverse: Boolean,
    val data: SimulationEntity? = null,
    val error: Exception? = null,
    val timeMillis: Long = System.currentTimeMillis(),
    val uptimeMillis: Long = android.os.SystemClock.uptimeMillis()
) : Parcelable {
    companion object {
        val EMPTY = SimulationResult(
            SwapEntity.EMPTY,
            SwapEntity.EMPTY,
            false,
            null,
            null,
            0,
            0
        )
    }

    @IgnoredOnParcel
    val isSuccessful: Boolean = data != null
    @IgnoredOnParcel
    val isEmpty: Boolean = data == null && error == null
    @IgnoredOnParcel
    val isError: Boolean = error != null

    fun belongsToTokens(send: TokenEntity, receive: TokenEntity, allowSwap: Boolean = false): Boolean {
        return (this.send.token == send && this.receive.token == receive) ||
            (allowSwap && this.receive.token == send && this.send.token == receive)
    }

    fun guessAmountOrNull(asking: TokenEntity, offer: TokenEntity, amount: BigDecimal): BigDecimal? {
        if (!isSuccessful || data!!.swapRate <= BigDecimal.ZERO)
            return null

        if ((send.token == offer && receive.token == asking) || (send.token == asking && receive.token == offer)) {
            val guess = when (asking) {
                receive.token -> amount
                    .multiply(data.swapRate)
                send.token -> amount
                    .divide(data.swapRate, asking.decimals, RoundingMode.DOWN)
                else -> null
            }
            return guess?.scaleDownAndStripTrailingZeros(asking.decimals)
        }

        return null
    }

    fun hasChanges(other: SimulationResult): Boolean {
        return this.send != other.send || this.receive != other.receive || this.data != other.data || this.error != other.error
    }
}

@Parcelize
data class SimulationDisplayData(
    val swapRate: String,
    val priceImpactPercentage: String,
    val minReceived: String,
    val fee: String,
    val blockchainFee: String,
    val route: String
) : Parcelable {
    constructor(format: NumberFormat, send: SwapEntity, receive: SwapEntity, data: SimulationEntity, request: SwapRequest?) : this(
        swapRate = "1 ${send.token!!.symbol} ≈ ${data.swapRate.toDisplayAmount(format, receive.token!!.decimals)} ${receive.token.symbol}",
        priceImpactPercentage = "${data.priceImpactPercentage.toDisplayAmount()} %",
        minReceived = data.minReceived.coins.toDisplayAmount(format, receive.token.decimals),
        fee = "${data.fee.coins.toDisplayAmount(format, receive.token.decimals)} ${receive.token.symbol}",
        blockchainFee = request?.operationDetails?.feesInTon?.stringRepresentation?.let {
            "≈ $it ${TokenEntity.TON.symbol}"
        } ?: "${SwapConfig.DEX_SWAP_ESTIMATED_FEE} ${TokenEntity.TON.symbol}",
        route = "${send.token.symbol} » ${receive.token.symbol}"
    )
}