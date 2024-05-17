package uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.uikit.color.textPrimaryColor
import uikit.R
import uikit.extensions.dp
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.setPaddingHorizontal

class LabelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.textViewStyle,
) : AppCompatTextView(context, attrs, defStyle) {

    init {
        setTextAppearance(R.style.TextAppearance_Label1)
        setTextColor(context.textPrimaryColor)
        gravity = Gravity.CENTER_VERTICAL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val labelHeight = context.getDimensionPixelSize(R.dimen.labelHeight)
        val heightSpec = MeasureSpec.makeMeasureSpec(labelHeight, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightSpec)
    }
}