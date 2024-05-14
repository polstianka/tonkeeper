package uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.tonapps.uikit.color.accentBlueColor
import uikit.R
import uikit.extensions.dp
import uikit.extensions.useAttributes

class ChipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    private val textView: TextView
    @ColorInt
    var color: Int = context.accentBlueColor
        set(value) {
            field = value
            textView.setTextColor(value)
            background.setTint(value)
        }

    init {
        background = ContextCompat.getDrawable(context, R.drawable.bg_chip)
        textView = TextView(context)
        textView.setTextAppearance(R.style.TextAppearance_Body4CAPS)
        val padding = 4f.dp.toInt()
        setPadding(padding, padding, padding, padding)
        addView(textView)
        context.useAttributes(attrs, R.styleable.ChipView) { attrs ->
            if (attrs.hasValue(R.styleable.ChipView_color)) {
                color = attrs.getColor(R.styleable.ChipView_color, 0)
            }
            if (attrs.hasValue(R.styleable.ChipView_text)) {
                textView.text = attrs.getText(R.styleable.ChipView_text)
            }
        }
    }
}