package uikit.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.math.MathUtils
import androidx.customview.widget.ViewDragHelper
import com.tonapps.uikit.color.textTertiaryColor
import com.tonapps.uikit.icon.UIKitIcon
import uikit.HapticHelper
import uikit.R
import uikit.extensions.dp
import uikit.extensions.getDimension
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.useAttributes

class SlideActionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    private companion object {
        private val arrowIcon = UIKitIcon.ic_arrow_right_outline_28
        private val checkIcon = UIKitIcon.ic_donemark_otline_28
    }

    var doOnDone: (() -> Unit)? = null

    var text: CharSequence?
        get() = textView.text
        set(value) {
            textView.text = value
        }

    private var icon: Int = arrowIcon
        set(value) {
            if (field != value) {
                field = value
                buttonView.setImageResource(value)
            }
        }

    private var isDone = false

    private val drawCallback = object : ViewDragHelper.Callback() {
        private val horizontalDragRange: Int
            get() = measuredWidth - buttonView.measuredWidth

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child == buttonView && !isDone && isEnabled
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return MathUtils.clamp(left, 0, horizontalDragRange)
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            return horizontalDragRange
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            val left = releasedChild.left
            if (left >= horizontalDragRange) {
                dragHelper.settleCapturedViewAt(left, 0)
                done()
            } else {
                dragHelper.settleCapturedViewAt(0, 0)
            }
            invalidate()
        }

        override fun onViewPositionChanged(
            changedView: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int
        ) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            textView.alpha = 1f - left / horizontalDragRange.toFloat()
            icon = if (left >= horizontalDragRange) {
                checkIcon
            } else {
                arrowIcon
            }
        }

        override fun onViewDragStateChanged(state: Int) {
            super.onViewDragStateChanged(state)
            when (state) {
                ViewDragHelper.STATE_IDLE -> {
                    parent.requestDisallowInterceptTouchEvent(false)
                }

                ViewDragHelper.STATE_DRAGGING -> {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
            }
        }
    }

    private val textView: AppCompatTextView
    private val buttonView: AppCompatImageView
    private val dragHelper: ViewDragHelper

    init {
        setBackgroundResource(R.drawable.bg_content)
        inflate(context, R.layout.view_slide_action, this)

        textView = findViewById(R.id.text)
        buttonView = findViewById(R.id.button)

        dragHelper = ViewDragHelper.create(this, 0.2f, drawCallback)
        dragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT or ViewDragHelper.EDGE_RIGHT)

        context.useAttributes(attrs, R.styleable.SlideActionView) {
            textView.text = it.getString(R.styleable.SlideActionView_android_text)
        }
    }

    fun reset() {
        isDone = false
        icon = arrowIcon
        dragHelper.smoothSlideViewTo(buttonView, 0, 0)
        invalidate()
    }

    private fun done() {
        HapticHelper.impactLight(context)
        icon = checkIcon
        doOnDone?.invoke()
        isDone = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = context.getDimensionPixelSize(R.dimen.itemHeight)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
        dragHelper.minVelocity = measuredWidth / 2f
    }

    override fun computeScroll() {
        super.computeScroll()
        if (dragHelper.continueSettling(true)) {
            postInvalidateOnAnimation()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return try {
            dragHelper.processTouchEvent(event)
            true
        } catch (e: Throwable) {
            super.onTouchEvent(event)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return try {
            dragHelper.shouldInterceptTouchEvent(ev)
        } catch (e: Throwable) {
            super.onInterceptTouchEvent(ev)
        }
    }

    override fun hasOverlappingRendering() = false

    class GradientTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = android.R.attr.textViewStyle,
    ) : AppCompatTextView(context, attrs, defStyle), ValueAnimator.AnimatorUpdateListener {

        private val gradientWidth = 84f.dp * 2
        private val color = Color.parseColor("#C2DAFF")
        private val textColor = context.textTertiaryColor
        private val gradientColors = intArrayOf(textColor, color, color, textColor)

        private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2400
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
            addUpdateListener(this@GradientTextView)
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            paint.shader = LinearGradient(
                0f,
                0f,
                w + gradientWidth,
                context.getDimension(R.dimen.itemHeight),
                gradientColors,
                null,
                Shader.TileMode.MIRROR
            )
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            val w = measuredWidth + gradientWidth
            val progress = animation.animatedValue as Float
            val matrix = Matrix()
            matrix.setTranslate(w * progress, 0f)
            paint.shader?.setLocalMatrix(matrix)
            invalidate()
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            animator.start()
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            animator.cancel()
        }
    }
}