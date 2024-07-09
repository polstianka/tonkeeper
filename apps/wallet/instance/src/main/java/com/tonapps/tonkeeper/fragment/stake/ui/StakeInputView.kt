package com.tonapps.tonkeeper.fragment.stake.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.AttrRes
import com.tonapps.tonkeeper.extensions.doOnAmountChange
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.resolveColor
import uikit.extensions.dp
import uikit.extensions.round
import uikit.extensions.setThrottleClickListener
import uikit.widget.ColumnLayout
import java.math.BigDecimal

class StakeInputView  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ColumnLayout(context, attrs, defStyle) {

    private val input: AmountInput?
        get() = findViewById(R.id.view_stake_input_input)
    private val fiat: TextView?
        get() = findViewById(R.id.view_stake_input_fiat)
    private val maxButton: View?
        get() = findViewById(R.id.view_stake_input_max_button)
    private val label: TextView?
        get() = findViewById(R.id.view_stake_input_label)
    private var onAmountChangedAction: ((BigDecimal) -> Unit)? = null
    private var onMaxClickedAction: (() -> Unit)? = null

    init {
        inflate(context, R.layout.view_stake_input, this)
        input?.doOnAmountChange { onAmountChangedAction?.invoke(it) }
        maxButton?.setThrottleClickListener { onMaxClickedAction?.invoke() }
        maxButton?.round(24f.dp.toInt())
    }

    fun setInputText(inputText: String) {
        input?.setText(inputText)
    }

    fun setOnAmountChangedListener(action: (BigDecimal) -> Unit) {
        onAmountChangedAction = action
    }

    fun setOnMaxClickedListener(action: () -> Unit) {
        onMaxClickedAction = action
    }

    fun setLabelText(text: String) {
        label?.text = text
    }

    fun setLabelTextColorAttribute(@AttrRes attr: Int) {
        label?.setTextColor(context.resolveColor(attr))
    }

    fun setFiatText(text: String) {
        fiat?.text = text
    }

    fun setMaxButtonActivated(isActivated: Boolean) {
        val bg = if (isActivated) {
            uikit.R.drawable.bg_button_primary
        } else {
            uikit.R.drawable.bg_button_secondary
        }
        maxButton?.setBackgroundResource(bg)
    }
}