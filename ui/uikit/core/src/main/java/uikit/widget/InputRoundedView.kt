package uikit.widget

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.tonapps.uikit.color.accentBlueColor
import com.tonapps.uikit.color.accentRedColor
import com.tonapps.uikit.color.textPrimaryColor
import com.tonapps.uikit.color.textSecondaryColor
import uikit.R
import uikit.drawable.InputAutoDrawable
import uikit.extensions.dp
import uikit.extensions.focusWithKeyboard
import uikit.extensions.setCursorColor
import uikit.extensions.setPaddingEnd
import uikit.extensions.setPaddingHorizontal
import uikit.extensions.useAttributes
import androidx.appcompat.R as appcompatR

class InputRoundedView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = appcompatR.attr.editTextStyle,
) : AppCompatEditText(context, attrs, defStyle),
    TextWatcher {

    private val inputDrawable = InputAutoDrawable(context)
    private var suffix: String = ""
    private var suffixSpacing = 0.dp

    var error: Boolean
        get() = inputDrawable.error
        set(value) {
            inputDrawable.error = value
            if (value) {
                setCursorColor(context.accentRedColor)
            } else {
                setCursorColor(context.accentBlueColor)
            }
        }

    var doOnTextChange: ((editable: Editable) -> Unit)? = null

    init {
        background = inputDrawable
        setPaddingHorizontal(16.dp)

        context.useAttributes(attrs, R.styleable.InputRoundedView) {
            suffix = it.getString(R.styleable.InputRoundedView_inputSuffixText) ?: suffix
            suffixSpacing = it.getDimensionPixelSize(
                R.styleable.InputRoundedView_inputSuffixSpacing,
                suffixSpacing
            )
        }

        setTextColor(context.textPrimaryColor)
        setHintTextColor(context.textSecondaryColor)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setTextCursorDrawable(R.drawable.cursor)
        } else {
            setCursorColor(context.accentBlueColor)
        }

        val endPadding = suffixSpacing + paint.measureText(suffix) + paddingEnd
        setPaddingEnd(endPadding.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val text = text?.toString() ?: ""
        val endTextX = if (text.isBlank()) {
            paint.measureText(hint.toString())
        } else {
            paint.measureText(text)
        }
        val saveColor = paint.color
        paint.setColor(currentHintTextColor)
        canvas.drawText(
            suffix,
            paddingLeft + endTextX + suffixSpacing,
            getBaseline().toFloat(),
            paint
        )
        paint.setColor(saveColor)
    }

    fun focus() {
        postDelayed({
            focusWithKeyboard()
        }, 16)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(editable: Editable) {
        doOnTextChange?.invoke(editable)
    }
}