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
import uikit.R
import uikit.drawable.BarDrawable
import uikit.drawable.HeaderDrawable
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.setPaddingHorizontal
import uikit.extensions.setPaddingTop
import uikit.extensions.useAttributes
import uikit.extensions.withAnimation

open class HeaderViewSimple @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RowLayout(context, attrs, defStyle), BarDrawable.BarDrawableOwner {

    private companion object {
        private const val ANIMATION_DURATION = 180L
    }

    val actionView: AppCompatImageView
    val titleView: AppCompatTextView

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
    private val textView: View

    var doOnActionClick: (() -> Unit)? = null
        set(value) {
            field = value
            actionView.setOnClickListener {
                if (it.alpha != 0f) {
                    value?.invoke()
                }
            }
        }


    var title: CharSequence
        get() = titleView.text
        set(value) {
            titleView.text = value
        }

    init {
        super.setBackground(drawable)
        setPaddingHorizontal(context.getDimensionPixelSize(R.dimen.offsetMedium))

        inflate(context, R.layout.view_header_simple, this)

        titleView = findViewById(R.id.header_title)
        actionView = findViewById(R.id.header_action)
        textView = findViewById(R.id.header_text)

        context.useAttributes(attrs, R.styleable.HeaderView) {
            ignoreSystemOffset = it.getBoolean(R.styleable.HeaderView_ignoreSystemOffset, false)
            titleView.text = it.getString(R.styleable.HeaderView_android_title)

            val actionResId = it.getResourceId(R.styleable.HeaderView_android_action, 0)
            setAction(actionResId)
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

    fun setAction(@DrawableRes resId: Int) {
        setDrawableForView(actionView, resId)
    }

    private fun setDrawableForView(view: AppCompatImageView, @DrawableRes resId: Int) {
        if (resId == 0) {
            view.alpha = 0f
        } else {
            view.setImageResource(resId)
            view.alpha = 1f
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(barHeight + topOffset, MeasureSpec.EXACTLY))
    }
}