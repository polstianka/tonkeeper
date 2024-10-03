package com.tonapps.tonkeeper.ui.component.coin

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.R
import androidx.core.widget.doAfterTextChanged
import com.tonapps.icu.Coins
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.extensions.setBigDecimal
import com.tonapps.tonkeeper.ui.component.coin.drawable.SuffixDrawable
import com.tonapps.tonkeeper.ui.component.coin.format.CoinFormattingConfig
import com.tonapps.tonkeeper.ui.component.coin.format.CoinFormattingFilter
import com.tonapps.tonkeeper.ui.component.coin.format.CoinFormattingTextWatcher
import uikit.extensions.dp
import uikit.extensions.replaceAll
import uikit.extensions.setRightDrawable
import uikit.widget.input.InputTextView
import java.math.BigDecimal

class CoinEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.editTextStyle,
) : InputTextView(context, attrs, defStyle) {

    private val suffixDrawable = SuffixDrawable(context)

    private lateinit var formattingConfig: CoinFormattingConfig

    var doOnValueChange: ((Double) -> Unit)? = null

    var suffix: String?
        get() = suffixDrawable.text
        set(value) {
            suffixDrawable.text = value
            invalidate()
        }

    init {
        setMaxLength(24)
        setRightDrawable(suffixDrawable)
        compoundDrawablePadding = 38.dp
        setDecimals(9)
        doAfterTextChanged {
            val value = getValue()
            doOnValueChange?.invoke(value)
        }
    }

    val decimals: Int
        get() = formattingConfig.decimals

    fun setDecimals(decimals: Int) {
        formattingConfig = CoinFormattingConfig(decimals = decimals)
        setFormattingTextWatcher(CoinFormattingTextWatcher(formattingConfig))
        setFormattingInputFilter(CoinFormattingFilter(formattingConfig))
    }

    fun getValue(): Double {
        val text = text.toString()
        if (text.isEmpty()) {
            return 0.0
        }
        return Coins.safeParseDouble(text)
    }

    fun setValue(value: Double) {
        val editable = getText() ?: return
        if (0 >= value) {
            clear()
        } else {
            editable.replaceAll(value.toString().removeSuffix(".0"))
        }
    }

    fun setValue(value: BigDecimal) {
        setBigDecimal(value)
    }

    fun clear() {
        text?.clear()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth + suffixDrawable.intrinsicWidth
        setMeasuredDimension(width, measuredHeight)
    }
}