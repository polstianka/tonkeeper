package uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.WindowInsetsCompat
import com.tonapps.uikit.color.backgroundTransparentColor
import uikit.R
import uikit.drawable.BarDrawable
import uikit.drawable.HeaderDrawable
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.setPaddingHorizontal
import uikit.extensions.setPaddingTop
import uikit.extensions.useAttributes
import uikit.extensions.withAnimation

open class HeaderViewEmpty @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RowLayout(context, attrs, defStyle), BarDrawable.BarDrawableOwner {
    private val barHeight = context.getDimensionPixelSize(R.dimen.barHeight)
    private var ignoreSystemOffset = false
    private var topOffset: Int = 0
        set(value) {
            if (field != value) {
                field = value
                setPaddingTop(value)
                requestLayout()
            }
        }

    private val drawable = HeaderDrawable(context)

    init {
        super.setBackground(drawable)
        setPaddingHorizontal(context.getDimensionPixelSize(R.dimen.offsetMedium))

        context.useAttributes(attrs, R.styleable.HeaderView) {
            ignoreSystemOffset = it.getBoolean(R.styleable.HeaderView_ignoreSystemOffset, false)
        }
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        if (ignoreSystemOffset) {
            return super.onApplyWindowInsets(insets)
        }
        val compatInsets = WindowInsetsCompat.toWindowInsetsCompat(insets)
        val statusInsets = compatInsets.getInsets(WindowInsetsCompat.Type.statusBars())
        topOffset = statusInsets.top
        return super.onApplyWindowInsets(insets)
    }

    override fun setDivider(value: Boolean) {
        drawable.setDivider(value)
    }

    fun setColor(color: Int) {
        drawable.setColor(color)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(barHeight + topOffset, MeasureSpec.EXACTLY))
    }
}