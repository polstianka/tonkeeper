package com.tonapps.tonkeeper.ui.component

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.WindowInsets
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import blur.BlurCompat
import com.tonapps.tonkeeper.isBlurDisabled
import com.tonapps.tonkeeper.koin.api
import com.tonapps.wallet.api.entity.FlagsEntity
import uikit.extensions.getDimensionPixelSize
import uikit.widget.SimpleRecyclerView

class MainRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : SimpleRecyclerView(context, attrs, defStyle) {

    private val paddingVertical = context.getDimensionPixelSize(uikit.R.dimen.offsetMedium)
    private val barSize = context.getDimensionPixelSize(uikit.R.dimen.barHeight)

    private var topOffset = 0
    private var bottomOffset = 0

    private val topPadding: Int
        get() = topOffset + barSize

    private val bottomPadding: Int
        get() = bottomOffset + barSize

    private val blurDisabled = context.isBlurDisabled
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
        val navigationInsets = compatInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
        topOffset = statusInsets.top
        bottomOffset = navigationInsets.bottom
        updatePadding(
            top = paddingVertical + topPadding,
            bottom = paddingVertical + bottomPadding
        )
        applyBlurBounds()
        return super.onApplyWindowInsets(insets)
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (topBlur == null) {
            super.dispatchDraw(canvas)
        } else {
            topBlur.draw(canvas) {
                super.dispatchDraw(it)
            }
        }
    }

    override fun draw(canvas: Canvas) {
        if (bottomBlur == null) {
            super.draw(canvas)
        } else {
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
        if (viewWidth == 0f || viewHeight == 0f) {
            return
        }
        topBlur?.setBounds(0f, 0f, viewWidth, topPadding.toFloat())
        bottomBlur?.setBounds(0f, viewHeight - bottomPadding, viewWidth, viewHeight)
    }
}