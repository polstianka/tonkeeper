package com.tonapps.tonkeeper.ui.component.swap

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.widget.doOnTextChanged
import com.tonapps.tonkeeper.extensions.amount
import com.tonapps.tonkeeper.fragment.swap.model.Simulate
import com.tonapps.tonkeeper.fragment.swap.model.SwapState
import com.tonapps.tonkeeperx.R
import uikit.widget.AmountInput

class SendSwapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : BaseSwapView(context, attrs, defStyle) {

    override fun layoutId() = R.layout.view_swap_send
    private val maxView: View = findViewById(R.id.max)
    private val valueView: AmountInput = findViewById(R.id.value)
    private val valueTextView: TextView = findViewById(R.id.valueText)

    var onAmountChange: ((Float) -> Unit)? = null
    private var amount: Float
        set(value) {
            valueView.setAmount(value)
        }
        get() = valueView.amount

    var onMaxClick: (() -> Unit)? = null

    init {
        maxView.setOnClickListener {
            onMaxClick?.invoke()
        }
        valueView.doOnTextChanged { _, _, _, _ ->
            onAmountChange?.invoke(amount)
        }
    }

    fun setConfirmMode(sendToken: SwapState.TokenState) {
        super.setTokenState(sendToken)
        maxView.isGone = true
        valueView.isGone = true
        valueTextView.isGone = false
        valueTextView.text = sendToken.value.amount
    }

    override fun setTokenState(tokenState: SwapState.TokenState?) {
        super.setTokenState(tokenState)
        if (tokenState != null) {
            amount = tokenState.value
        }
    }

    override fun contentViews(): List<View> {
        return super.contentViews().plus(listOf(maxView, valueView))
    }
}