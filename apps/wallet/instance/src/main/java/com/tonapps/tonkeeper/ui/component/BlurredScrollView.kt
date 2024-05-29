package com.tonapps.tonkeeper.ui.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.WindowInsets
import android.widget.ScrollView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import blur.BlurCompat
import com.tonapps.tonkeeper.isBlurDisabled
import com.tonapps.uikit.color.backgroundTransparentColor
import uikit.R
import uikit.extensions.useAttributes
import kotlin.math.max

class BlurredScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ScrollView(context, attrs, defStyle) {

    var unblurredPaddingTop = 0
        set(value) {
            field = value
            update()
        }

    var unblurredPaddingBottom = 0
        set(value) {
            field = value
            update()
        }

    var blurredPaddingTop = 0
        set(value) {
            field = value
            update()
        }

    var blurredPaddingBottom = 0
        set(value) {
            field = value
            update()
        }



    private var ignoreSystemTopOffset = false
    private var ignoreSystemBottomOffset = false
    private var bgPaint = Paint()

    init {
        context.useAttributes(attrs, R.styleable.BlurredRecyclerView) {
            ignoreSystemTopOffset = it.getBoolean(R.styleable.BlurredRecyclerView_ignoreTopSystemOffset, true)
            ignoreSystemBottomOffset = it.getBoolean(R.styleable.BlurredRecyclerView_ignoreBottomSystemOffset, false)
        }

        bgPaint.color = context.backgroundTransparentColor
        clipToPadding = false
    }


    private var topOffset = 0
    private var bottomOffset = 0

    private val topPadding: Int
        get() = topOffset + blurredPaddingTop

    private val bottomPadding: Int
        get() = bottomOffset + blurredPaddingBottom

    var blurDisabled = context.isBlurDisabled
        set(value) {
            field = value && context.isBlurDisabled
        }

    private val topBlur: BlurCompat? = if (!blurDisabled) BlurCompat(context) else null
    private val bottomBlur: BlurCompat? = if (!blurDisabled) BlurCompat(context) else null

    init {
        if (bottomBlur?.hasBlur == true) {
            overScrollMode = OVER_SCROLL_NEVER
        }
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        val compatInsets = WindowInsetsCompat.toWindowInsetsCompat(insets)
        val statusInsets = compatInsets.getInsets(WindowInsetsCompat.Type.statusBars())
        //val navigationInsets = compatInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
        val navigationInsets = compatInsets.getInsets(WindowInsetsCompat.Type.ime())

        topOffset = if (!ignoreSystemTopOffset) statusInsets.top else 0
        bottomOffset = if (!ignoreSystemBottomOffset) max(navigationInsets.bottom, compatInsets.stableInsetBottom) else 0
        update()
        return super.onApplyWindowInsets(insets)
    }

    private fun update() {
        updatePadding(
            top = topPadding + unblurredPaddingTop,
            bottom = bottomPadding + unblurredPaddingBottom
        )
        applyBlurBounds()
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (topBlur == null || blurDisabled) {
            super.dispatchDraw(canvas)
        } else {
            val viewWidth = measuredWidth.toFloat()
            topBlur.setBounds( 0f, scrollY.toFloat(), viewWidth, topPadding.toFloat() + scrollY)
            topBlur.draw(canvas) {
                super.dispatchDraw(it)
            }
        }
    }

    override fun draw(canvas: Canvas) {
        if (bottomBlur == null || blurDisabled) {
            super.draw(canvas)
        } else {
            val viewWidth = measuredWidth.toFloat()
            val viewHeight = measuredHeight.toFloat()
            bottomBlur.setBounds(0f, viewHeight - bottomPadding + scrollY, viewWidth, viewHeight + scrollY)
            bottomBlur.draw(canvas) {
                super.draw(it)
            }
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        applyBlurBounds()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        topBlur?.attached()
        bottomBlur?.attached()
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        topBlur?.detached()
        bottomBlur?.detached()
    }

    private fun applyBlurBounds() {
        val viewWidth = measuredWidth.toFloat()
        val viewHeight = measuredHeight.toFloat()
        topBlur?.setBounds( 0f, 0f, viewWidth, topPadding.toFloat())
        bottomBlur?.setBounds(0f, viewHeight - bottomPadding, viewWidth, viewHeight)
    }

    /*
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            if (ev.y < paddingTop || ev.y > (measuredHeight - paddingBottom)) {
                return false
            }
        }
        return super.dispatchTouchEvent(ev)
    }
    */
}