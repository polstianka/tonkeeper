package com.tonapps.tonkeeper.core.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatEditText
import com.tonapps.extensions.locale
import com.tonapps.tonkeeper.core.modifyInputAmount
import com.tonapps.tonkeeper.core.parseBigDecimal
import com.tonapps.tonkeeper.core.setTypedValue
import com.tonapps.tonkeeper.core.text.CharLimitInputFilter
import com.tonapps.tonkeeper.core.toDisplayAmount
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapConfig
import com.tonapps.uikit.color.UIKitColor
import uikit.animator.ArgbAnimator
import uikit.extensions.cursorToEnd
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat

class AmountInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = androidx.appcompat.R.attr.editTextStyle,
) : AppCompatEditText(context, attrs, defStyle), TextWatcher {
    companion object {
        const val DECIMALS_UNSPECIFIED = -1
        private const val LOGGING_ENABLED = SwapConfig.DEBUG_AMOUNT_INPUT
        private const val LOGGING_TAG = SwapConfig.LOGGING_TAG
    }

    private val locale = context.locale
    private val numberFormat = NumberFormat.getNumberInstance(locale)
    private val decimalSeparator
        get() = if (numberFormat is DecimalFormat) numberFormat.decimalFormatSymbols.decimalSeparator else '.'

    private val originalTextSize: Float

    init {
        addTextChangedListener(this)
        if (numberFormat is DecimalFormat) {
            filters = arrayOf(CharLimitInputFilter(1) {
                it == decimalSeparator
            })
        }
        originalTextSize = textSize
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

    override fun onTextChanged(
        text: CharSequence,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
    }

    private var onAmountChange: (view: AmountInputView, amount: BigDecimal, formatted: String) -> Unit = { _, _, _ -> }

    fun doOnDecimalValueChange(onAmountChange: (view: AmountInputView, amount: BigDecimal, formatted: String) -> Unit) {
        this.onAmountChange = onAmountChange
    }

    private fun String.fractionDigitsCount(numberFormat: NumberFormat? = null): Int {
        var decimalSeparator = '.'
        if (numberFormat is DecimalFormat) {
            val decimalFormatSymbols = numberFormat.decimalFormatSymbols
            decimalSeparator = decimalFormatSymbols.decimalSeparator
        }
        var count = 0
        var index = this.length
        while (--index >= 0) {
            val char = this[index]
            if (char.isDigit()) {
                count++
            } else if (char == decimalSeparator) {
                return count
            }
        }
        return 0
    }

    override fun afterTextChanged(editable: Editable) {
        if (ignoreTextUpdates) return
        ignoreTextUpdates = true
        val currentText = editable.toString()
        if (decimals != DECIMALS_UNSPECIFIED) {
            numberFormat.minimumFractionDigits = minOf(decimals, currentText.fractionDigitsCount(numberFormat))
        } else {
            numberFormat.minimumFractionDigits = currentText.fractionDigitsCount(numberFormat)
        }
        val parsed: BigDecimal? = currentText.parseBigDecimal(numberFormat, BigDecimal.ZERO)?.let {
            if (decimals != DECIMALS_UNSPECIFIED && it.scale() > decimals) {
                it.setScale(decimals, RoundingMode.DOWN)
            } else {
                it
            }
        }
        if (LOGGING_ENABLED) {
            Log.w(LOGGING_TAG, "$locale($decimalSeparator) text: '$currentText' format: '$parsed'")
        }
        isFormatError = parsed == null
        val oldValue = decimalValue
        var newText = currentText
        val newValue = if (parsed != null) {
            val replacementText = currentText.modifyInputAmount(parsed, numberFormat)
            if (currentText != replacementText) {
                if (LOGGING_ENABLED) {
                    Log.w(LOGGING_TAG, "$locale($decimalSeparator) replacing '$currentText' with '$replacementText'")
                }
                editable.replace(0, editable.length, replacementText)
                newText = replacementText
            }
            parsed.stripTrailingZeros()
        } else {
            BigDecimal.ZERO
        }
        formattedValue = newText
        decimalValue = newValue
        ignoreTextUpdates = false
        if (oldValue != decimalValue) {
            onAmountChange(this, newValue, newText)
        }
    }

    var decimals: Int = DECIMALS_UNSPECIFIED
        set(value) {
            if (field == value) return
            field = value
            val format = numberFormat
            if (format is DecimalFormat) {
                format.maximumFractionDigits = decimals
                format.roundingMode = RoundingMode.DOWN
            }
        }

    private var ignoreTextUpdates: Boolean = false

    var formattedValue: String = ""
        private set(value) {
            if (field != value) {
                field = value
                updateTextSize()
            }
        }
    var decimalValue: BigDecimal = BigDecimal.ZERO
        set(value) {
            if (field == value) return
            field = value
            if (!ignoreTextUpdates) {
                numberFormat.minimumFractionDigits = 0
                val text = value.toDisplayAmount(numberFormat)
                ignoringTextChanges {
                    changeText(text)
                }
                onAmountChange(this, value, text)
            }
        }

    private fun ignoringTextChanges (block: () -> Unit) {
        val oldValue = ignoreTextUpdates
        ignoreTextUpdates = true
        block()
        ignoreTextUpdates = oldValue
    }

    fun setDecimalValueOptimized(decimal: BigDecimal, formatted: String) {
        ignoringTextChanges {
            decimalValue = decimal
            changeText(formatted)
        }
    }

    private fun changeText(text: String) {
        if (this.text.toString() != text) {
            this.setText(text)
            formattedValue = text
            if (isFocused) {
                cursorToEnd()
            }
        }
    }

    private val textColorAnimator = ArgbAnimator(122L, DecelerateInterpolator(), currentTextColor) {
        setTextColor(it)
    }

    private var hasAnyError: Boolean = false
        set(value) {
            if (field == value) return
            field = value

            val colorRef = if (hasAnyError) {
                UIKitColor.fieldErrorBorderColor // TODO(design): separate color id
            } else {
                UIKitColor.textPrimaryColor
            }
            animateToTypedColor(colorRef)
        }

    var isFormatError: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            hasAnyError = value || isInsufficient
        }

    var isInsufficient: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            hasAnyError = value || isFormatError
        }

    var animationsEnabled: () -> Boolean = { true }

    @AttrRes
    private var textColorRef: Int = UIKitColor.textPrimaryColor

    fun animateToArgbColor(@ColorInt argb: Int) {
        textColorRef = 0
        textColorAnimator.changeValue(argb, animationsEnabled())
    }

    fun animateToTypedColor(@AttrRes newColorRef: Int) {
        if (newColorRef != textColorRef) {
            textColorRef = newColorRef
            textColorAnimator.setTypedValue(context, newColorRef, animationsEnabled())
        }
    }

    @SuppressWarnings("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Workaround for ScrollView+EditText bug.
        if (event.action == MotionEvent.ACTION_DOWN && !isEnabled) return false
        return super.onTouchEvent(event)
    }

    var enableAutoFit: Boolean = false

    override fun setAutoSizeTextTypeUniformWithPresetSizes(presetSizes: IntArray, unit: Int) {
        super.setAutoSizeTextTypeUniformWithPresetSizes(presetSizes, unit)
    }

    private fun measureText(textSizePx: Float = paint.textSize): Float {
        if (formattedValue.isEmpty()) {
            return 0.0f
        }
        val oldTextSize = paint.textSize
        paint.textSize = textSizePx
        val result = paint.measureText(formattedValue)
        paint.textSize = oldTextSize
        return result
    }

    private fun applyTextSize(px: Float) {
        if (paint.textSize != px) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, px)
        }
    }

    private var lastViewportWidth: Int = 0
    private var lastTextWidth: Float = 0.0f

    private val textSizeStepPx: Float by lazy {
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 1.0f, resources.displayMetrics)
    }

    private fun updateTextSize() {
        if (!enableAutoFit) return
        val availableViewportWidth = measuredWidth - paddingLeft - paddingRight
        val currentTextSize = paint.textSize

        if (availableViewportWidth <= 0) {
            if (currentTextSize != originalTextSize) {
                applyTextSize(originalTextSize)
            }
            return
        }

        /*val originalTextWidth = measureText(originalTextSize)
        if (originalTextWidth == 0.0f || originalTextWidth <= availableViewportWidth) {
            if (currentTextSize != originalTextSize) {
                applyTextSize(originalTextSize)
            }
        } else {
            val scaledTextSize = (availableViewportWidth.toFloat() / originalTextWidth) * originalTextSize
            if (currentTextSize != scaledTextSize) {
                applyTextSize(scaledTextSize)
            }
        }*/

        val viewportSizeChanged = availableViewportWidth != lastViewportWidth
        lastViewportWidth = availableViewportWidth

        val currentTextWidth = measureText()
        val prevTextWidth = lastTextWidth
        lastTextWidth = currentTextWidth

        if (currentTextWidth == prevTextWidth) return

        val step = textSizeStepPx
        val minTextSize = step

        if (currentTextWidth == 0.0f) {
            applyTextSize(originalTextSize)
        } else if (currentTextWidth > prevTextWidth) {
            // text width increased, make sure we don't exceed the width
            if (currentTextWidth <= availableViewportWidth) return
            var delta = 0.0f
            while (currentTextSize - delta - step >= minTextSize) {
                delta += step
                val smallerWidth = measureText(currentTextSize - delta)
                if (smallerWidth <= availableViewportWidth || currentTextSize - delta == minTextSize) {
                    applyTextSize(currentTextSize - delta)
                    lastTextWidth = smallerWidth
                    return
                }
            }
            if (currentTextSize != minTextSize) {
                applyTextSize(minTextSize)
                lastTextWidth = measureText(minTextSize)
            }
        } else if (currentTextWidth < prevTextWidth) {
            // text width decreased, enlarge back if possible
            if (currentTextSize >= originalTextSize) return

            var newTextSize = currentTextSize
            var newTextWidth = currentTextWidth
            var delta = 0.0f
            while (currentTextSize + delta + step <= originalTextSize) {
                delta += step
                val biggerWidth = measureText(currentTextSize + delta)
                if (biggerWidth <= availableViewportWidth) {
                    newTextSize = currentTextSize + delta
                    newTextWidth = biggerWidth
                }
            }
            if (newTextSize != currentTextSize) {
                applyTextSize(newTextSize)
                lastTextWidth = newTextWidth
            }
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (width != oldWidth) {
            updateTextSize()
        }
    }

    /*var enableAutoFit: Boolean = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!enableAutoFit) return

        val viewportWidth = (parent as? View)?.measuredWidth?.toFloat() ?: 0.0f
        if (viewportWidth == 0.0f) return
        val viewWidth = measuredWidth
        if (viewWidth <= viewportWidth) {
            this.scale = 1.0f
        } else {
            val scale = viewportWidth / viewWidth
            val anchorX: Float = when (textAlignment) {
                TEXT_ALIGNMENT_VIEW_END,
                TEXT_ALIGNMENT_TEXT_END -> {
                    if (layoutDirection == LAYOUT_DIRECTION_LTR) {
                        viewportWidth - paddingRight
                    } else {
                        paddingLeft.toFloat()
                    }
                }
                TEXT_ALIGNMENT_TEXT_START,
                TEXT_ALIGNMENT_VIEW_START -> {
                    if (layoutDirection == LAYOUT_DIRECTION_LTR) {
                        paddingLeft.toFloat()
                    } else {
                        viewportWidth - paddingRight
                    }
                }
                // TEXT_ALIGNMENT_CENTER,
                else -> {
                    paddingLeft + (viewportWidth - paddingLeft - paddingRight) / 2.0f
                }
            }
            this.scale = scale
            this.pivotX = anchorX
            this.pivotY = pivotY
        }
    }*/
}