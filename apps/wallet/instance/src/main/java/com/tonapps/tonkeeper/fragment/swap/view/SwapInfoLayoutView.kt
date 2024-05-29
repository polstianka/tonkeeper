package com.tonapps.tonkeeper.fragment.swap.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.LinearLayoutCompat
import com.tonapps.tonkeeper.extensions.getPriceImpactColor
import com.tonapps.tonkeeper.fragment.swap.SwapScreenViewModel
import com.tonapps.tonkeeperx.R

class SwapInfoLayoutView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyle) {

    private val priceImpact: SwapInfoRowView
    private val minimumReceived: SwapInfoRowView
    private val liquidityProviderFee: SwapInfoRowView
    private val blockchainFee: SwapInfoRowView
    private val route: SwapInfoRowView
    private val provider: SwapInfoRowView

    init {
        inflate(context, R.layout.view_swap_info_layout, this)
        orientation = VERTICAL
        priceImpact = findViewById(R.id.swap_info_price_impact)
        minimumReceived = findViewById(R.id.swap_info_minimum_received)
        liquidityProviderFee = findViewById(R.id.swap_info_liquidity_provider_fee)
        blockchainFee = findViewById(R.id.swap_info_blockchain_fee)
        route = findViewById(R.id.swap_info_route)
        provider = findViewById(R.id.swap_info_provider)
    }

    fun set(info: SwapScreenViewModel.SimulateSwapResult) {
        priceImpact.setValue("%.2f%%".format(info.priceImpact * 100f))
        priceImpact.setValueColorInt(context.getPriceImpactColor(info.priceImpact))
        minimumReceived.setValue(info.minReceiveAmountFmt)
        liquidityProviderFee.setValue(info.liquidityFeeAmountFmt)
        blockchainFee.setValue("0.08 - 0.25 TON")
        route.setValue(info.tokenToSend.token.symbol + " » " + info.tokenToReceive.token.symbol)
        provider.setValue("STON.fi")
    }
}