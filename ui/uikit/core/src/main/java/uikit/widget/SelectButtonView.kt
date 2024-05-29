package uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.uikit.color.textPrimaryColor
import uikit.R
import uikit.drawable.SelectButtonDrawable
import uikit.extensions.dp

class SelectButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.textViewStyle,
) : AppCompatTextView(context, attrs, defStyle) {

    init {
        setTextAppearance(R.style.TextAppearance_Body1)
        setTextColor(context.textPrimaryColor)
        setBackgroundDrawable(SelectButtonDrawable(context))
        gravity = Gravity.CENTER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = 56.dp
        val heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightSpec)
    }
}