package uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.core.view.WindowInsetsCompat
import uikit.R
import uikit.drawable.BarDrawable
import uikit.drawable.FooterDrawable
import uikit.extensions.setPaddingBottom
import uikit.extensions.useAttributes
import kotlin.math.max

open class FooterViewEmpty @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle), BarDrawable.BarDrawableOwner {
    private var ignoreSystemOffset = false
    private var bottomOffset: Int = 0
        set(value) {
            if (field != value) {
                field = value
                setPaddingBottom(value)
                requestLayout()
            }
        }

    val drawable = FooterDrawable(context)

    init {
        super.setBackground(drawable)
        context.useAttributes(attrs, R.styleable.HeaderView) {
            ignoreSystemOffset = it.getBoolean(R.styleable.HeaderView_ignoreSystemOffset, false)
        }
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        if (ignoreSystemOffset) {
            return super.onApplyWindowInsets(insets)
        }
        val compatInsets = WindowInsetsCompat.toWindowInsetsCompat(insets)
        val navigationInsets = compatInsets.getInsets(WindowInsetsCompat.Type.ime())

        bottomOffset = max(navigationInsets.bottom, compatInsets.stableInsetBottom)
        return super.onApplyWindowInsets(insets)
    }

    override fun setDivider(value: Boolean) {
        drawable.setDivider(value)
    }

    fun setColor(color: Int) {
        drawable.setColor(color)
    }
}