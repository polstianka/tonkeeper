package com.tonapps.wallet.data.token.entities


import android.os.Parcelable
import io.tonapi.models.SwapSimulateDetail
import kotlinx.parcelize.Parcelize
import java.math.BigInteger
import java.text.DecimalFormat

@Parcelize
data class SwapSimulateEntity(

    val askAddress: String,

    val askUnits: BigInteger,

    val feeAddress: String,

    val feePercent: String,

    val feeUnits: BigInteger,

    val minAskUnits: BigInteger,

    val offerAddress: String,

    val offerUnits: BigInteger,

    val poolAddress: String,

    val priceImpact: String,

    val routerAddress: String,

    val slippageTolerance: String,

    val swapRate: String,

    var fromDecimals: Int? = null,

    var toDecimals: Int? = null

) : Parcelable {

    fun getPriceImpactAsFloat() =
        try {
            priceImpact.toFloat() * 100
        } catch (e: NumberFormatException) {
            0.0f
        }

    fun getFormattedPriceImpact() =
        "${DecimalFormat("0.00").format(getPriceImpactAsFloat())} %"


    constructor(
        swapSimulateDetail: SwapSimulateDetail
    ) : this(
        swapSimulateDetail.askAddress,
        swapSimulateDetail.askUnits,
        swapSimulateDetail.feeAddress,
        swapSimulateDetail.feePercent,
        swapSimulateDetail.feeUnits,
        swapSimulateDetail.minAskUnits,
        swapSimulateDetail.offerAddress,
        swapSimulateDetail.offerUnits,
        swapSimulateDetail.poolAddress,
        swapSimulateDetail.priceImpact,
        swapSimulateDetail.routerAddress,
        swapSimulateDetail.slippageTolerance,
        swapSimulateDetail.swapRate

    )

}