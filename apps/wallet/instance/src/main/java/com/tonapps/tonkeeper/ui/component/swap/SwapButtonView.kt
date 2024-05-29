package com.tonapps.tonkeeper.ui.component.swap

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import com.tonapps.tonkeeperx.R
import uikit.extensions.dp
import uikit.extensions.round

class SwapButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), AnimatorUpdateListener {

    private val arrowUp: View
    private val arrowDown: View

    private var moveDistance: Float = 0f
    init {
        inflate(context, R.layout.view_swap_button, this)

        setBackgroundResource(uikit.R.drawable.bg_oval_button_tertiary)

        arrowUp = findViewById(R.id.arrow_up)
        arrowDown = findViewById(R.id.arrow_down)

        round(SIZE / 2)
    }

    private val moveAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener(this@SwapButtonView)
    }

    fun animateMove() {
        moveAnimator.start()
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val progress = animation.animatedValue as Float

        if (progress < 0.5f) {
            stepOut(progress * 2.0f)
        } else {
            stepIn((progress - 0.5f) * 2.0f)
        }
    }

    private fun stepOut(progress: Float) {
        arrowUp.translationY = -moveDistance * progress
        arrowDown.translationY = moveDistance * progress
    }

    private fun stepIn(progress: Float) {
        val reverseProgress = 1.0f - progress
        arrowUp.translationY = moveDistance * reverseProgress
        arrowDown.translationY = -moveDistance * reverseProgress
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val sizeSpec = MeasureSpec.makeMeasureSpec(SIZE, MeasureSpec.EXACTLY)
        super.onMeasure(sizeSpec, sizeSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val height = measuredHeight
        val targetHeight = arrowUp.measuredHeight

        moveDistance = (height - targetHeight) / 2.0f + targetHeight

    }

    companion object {
        val SIZE = 40.dp
    }

}