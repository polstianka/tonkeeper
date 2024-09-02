package uikit.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Button
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.WindowInsetsCompat
import uikit.R
import uikit.drawable.BarDrawable
import uikit.drawable.HeaderDrawable
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.setPaddingHorizontal
import uikit.extensions.setPaddingTop
import uikit.extensions.useAttributes
import uikit.extensions.withAnimation

open class HeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RowLayout(context, attrs, defStyle), BarDrawable.BarDrawableOwner {

    private companion object {
        private const val ANIMATION_DURATION = 180L
    }

    val closeView: AppCompatImageView
    val actionView: AppCompatImageView
    val titleView: AppCompatTextView

    private val rightContentView: LinearLayoutCompat

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
    private val subtitleContainerView: View
    val subtitleView: AppCompatTextView
    private val loaderView: LoaderView
    private val textView: View

    var doOnCloseClick: (() -> Unit)? = null
        set(value) {
            field = value
            closeView.setOnClickListener {
                if (it.alpha != 0f) {
                    value?.invoke()
                }
            }
        }

    var doOnActionClick: ((view: View) -> Unit)? = null
        set(value) {
            field = value
            actionView.setOnClickListener {
                if (it.alpha != 0f) {
                    value?.invoke(it)
                }
            }
        }


    var title: CharSequence?
        get() = titleView.text
        set(value) {
            titleView.text = value
        }

    init {
        super.setBackground(drawable)
        setPaddingHorizontal(context.getDimensionPixelSize(R.dimen.offsetMedium))

        inflate(context, R.layout.view_header, this)

        subtitleContainerView = findViewById(R.id.subtitle_container)
        closeView = findViewById(R.id.header_close)
        titleView = findViewById(R.id.header_title)
        actionView = findViewById(R.id.header_action)
        subtitleView = findViewById(R.id.header_subtitle)
        loaderView = findViewById(R.id.header_loader)
        textView = findViewById(R.id.header_text)
        rightContentView = findViewById(R.id.header_right_content)

        context.useAttributes(attrs, R.styleable.HeaderView) {
            ignoreSystemOffset = it.getBoolean(R.styleable.HeaderView_ignoreSystemOffset, false)
            val iconResId = it.getResourceId(R.styleable.HeaderView_android_icon, 0)
            setIcon(iconResId)

            val iconTintColor = it.getColor(R.styleable.HeaderView_android_iconTint, Color.TRANSPARENT)
            if (iconTintColor != Color.TRANSPARENT) {
                closeView.setColorFilter(iconTintColor)
            }

            titleView.text = it.getString(R.styleable.HeaderView_android_title)

            val actionResId = it.getResourceId(R.styleable.HeaderView_android_action, 0)
            setAction(actionResId)
        }
    }

    fun setIgnoreSystemOffset(value: Boolean = true) {
        ignoreSystemOffset = value
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

    fun setIcon(@DrawableRes resId: Int) {
        setDrawableForView(closeView, resId)
    }

    fun setAction(@DrawableRes resId: Int) {
        setDrawableForView(actionView, resId)
    }

    fun setRightContent(view: View?) {
        rightContentView.removeAllViews()
        if (view != null) {
            rightContentView.visibility = View.VISIBLE
            rightContentView.addView(view)
        } else {
            rightContentView.visibility = View.GONE
        }
    }

    fun contentMatchParent() {
        titleView.layoutParams = titleView.layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        subtitleContainerView.layoutParams = subtitleContainerView.layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        subtitleView.layoutParams = subtitleView.layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    private fun setDrawableForView(view: AppCompatImageView, @DrawableRes resId: Int) {
        if (resId == 0) {
            view.alpha = 0f
            view.setOnClickListener(null)
        } else {
            view.setImageResource(resId)
            view.alpha = 1f
        }
    }

    fun setUpdating(@StringRes textResId: Int) {
        setSubtitle(textResId)
        loaderView.visibility = View.VISIBLE
        loaderView.startAnimation()
    }

    fun setDefault() {
        withAnimation(duration = ANIMATION_DURATION) {
            subtitleContainerView.visibility = View.GONE
            loaderView.visibility = View.GONE
            loaderView.stopAnimation()
        }
    }

    fun setSubtitle(@StringRes textResId: Int) {
        setSubtitle(context.getString(textResId))
    }

    fun setSubtitle(text: CharSequence?) {
        withAnimation(duration = ANIMATION_DURATION) {
            subtitleContainerView.visibility = if (text.isNullOrEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
            subtitleView.text = text
        }
    }

    fun hideIcon() {
        closeView.visibility = View.GONE
    }

    fun setTitleGravity(gravity: Int) {
        val layoutParams = titleView.layoutParams as LinearLayoutCompat.LayoutParams
        layoutParams.gravity = gravity
        titleView.gravity = gravity
    }

    fun hideText() {
        withAnimation(duration = ANIMATION_DURATION) {
            textView.alpha = 0f
        }
    }

    fun showText() {
        withAnimation(duration = ANIMATION_DURATION) {
            textView.alpha = 1f
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(barHeight + topOffset, MeasureSpec.EXACTLY))
    }

}