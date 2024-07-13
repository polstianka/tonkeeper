package com.tonapps.tonkeeper.ui.screen.swap

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.swap.StonfiSwapAsset
import com.tonapps.tonkeeper.api.swap.SwapRepository
import com.tonapps.tonkeeper.api.swap.SwapSimulateData
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.UIKitColor
import com.tonapps.uikit.color.resolveColor
import com.tonapps.wallet.localization.Localization
import uikit.widget.RowLayout

@SuppressLint("ClickableViewAccessibility")
class SwapSimulationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RowLayout(context, attrs, defStyle) {

    private var priceImpactValue: AppCompatTextView
    private var priceImpactText: AppCompatTextView
    private var minimumReceivedValue: AppCompatTextView
    private var minimumReceivedText: AppCompatTextView
    private var providerFeeValue: AppCompatTextView
    private var providerFeeText: AppCompatTextView
    private var blockchainFeeValue: AppCompatTextView
    private var routeValue: AppCompatTextView
    private var providerValue: AppCompatTextView
    private var hintListener: ((String) -> Unit)? = null

    init {
        inflate(context, R.layout.view_swap_simulation, this)
        priceImpactValue = findViewById(R.id.price_impact_text_value)
        minimumReceivedValue = findViewById(R.id.minimum_received_text_value)
        providerFeeValue = findViewById(R.id.provider_fee_text_value)
        blockchainFeeValue = findViewById(R.id.blockchain_fee_text_value)
        routeValue = findViewById(R.id.route_text_value)
        providerValue = findViewById(R.id.provider_text_value)

        priceImpactText = findViewById(R.id.price_impact_text)
        minimumReceivedText = findViewById(R.id.minimum_received_text)
        providerFeeText = findViewById(R.id.provider_fee_text)

        priceImpactText.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= priceImpactText.right - priceImpactText.totalPaddingRight) {
                        hintListener?.invoke(App.instance.getString(Localization.price_impact_hint))
                    }
                }
                return true;
            }
        })

        minimumReceivedText.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= minimumReceivedText.right - minimumReceivedText.totalPaddingRight) {
                        hintListener?.invoke(App.instance.getString(Localization.minimum_received_hint))
                    }
                }
                return true;
            }
        })

        providerFeeText.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= providerFeeText.right - providerFeeText.totalPaddingRight) {
                        hintListener?.invoke(App.instance.getString(Localization.provider_fee_hint))
                    }
                }
                return true;
            }
        })
    }

    fun setHintListener(listener: ((String) -> Unit)?) {
        hintListener = listener
    }

    @SuppressLint("SetTextI18n")
    fun setData(data: SwapSimulateData, from: StonfiSwapAsset, to: StonfiSwapAsset, slip: Float) {
        val priceImpact = data.priceImpact.toFloatOrNull() ?: 0f
        val prob = '\u2009'
        if (priceImpact <= 0.00001f) {
            priceImpactValue.setTextColor(Color.parseColor("#49C08A"))
        } else if (priceImpact >= slip) {
            priceImpactValue.setTextColor(context.resolveColor(UIKitColor.fieldErrorBorderColor))
        } else {
            priceImpactValue.setTextColor(context.resolveColor(UIKitColor.textPrimaryColor))
        }
        priceImpactValue.text = if (priceImpact <= 0.00001f) "0$prob%" else "${
            "%.2f".format(priceImpact * 100f).replace(',', '.')
        }$prob%"
        minimumReceivedValue.text = CurrencyFormatter.format(
            to.symbol,
            Coin.toCoins(data.minAskUnits.toLongOrNull() ?: 0L, to.decimals)
        )
        providerFeeValue.text =
            "${Coin.parseJettonBalance(data.feeUnits, to.decimals)} ${to.symbol}"
        blockchainFeeValue.text =
            "${Coin.parseJettonBalance(data.feePercent, from.decimals)} ${from.symbol}"
        providerValue.text = SwapRepository.PROVIDER_NAME
        routeValue.text = "${from.symbol} » ${to.symbol}"
    }
}