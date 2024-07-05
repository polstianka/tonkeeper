package uikit.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.graphics.withSave
import com.tonapps.uikit.color.accentGreenColor
import com.tonapps.uikit.color.fieldActiveBorderColor
import com.tonapps.uikit.color.fieldBackgroundColor
import com.tonapps.uikit.color.fieldErrorBorderColor
import uikit.ArgbEvaluator
import uikit.R
import uikit.extensions.dp
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.hapticConfirm
import uikit.extensions.hapticReject
import uikit.interpolator.ReverseInterpolator

class PinInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle) {

    companion object {
        const val animationDuration = 120L
    }

    private enum class DotState {
        DEFAULT,
        ACTIVE,
        ERROR,
        SUCCESS
    }

    private enum class PinCodeState {
        DEFAULT,
        ERROR,
        SUCCESS
    }

    private val numbers = mutableListOf<Int>()
    private val defaultColor = context.fieldBackgroundColor
    private val activeColor = context.fieldActiveBorderColor
    private val errorColor = context.fieldErrorBorderColor
    private var doneColor = context.accentGreenColor
    private val dotSize = 12f.dp
    private val dotActiveSize = 16f.dp
    private val dotActiveScale = dotActiveSize / dotSize
    private val dotGap = context.getDimensionPixelSize(R.dimen.offsetMedium)
    private val dots = arrayOf(Dot(), Dot(), Dot(), Dot())
    private var state = PinCodeState.DEFAULT
    private var currentCount = 0

    val code: String
        get() = numbers.joinToString("")

    val count: Int
        get() = numbers.size

    var doOnCodeUpdated: ((code: String) -> Unit)? = null

    fun appendNumber(number: Int) {
        if (currentCount == dots.size) {
            return
        }

        numbers.add(number)
        setCount(numbers.size)

        doOnCodeUpdated?.invoke(code)
    }

    fun removeLastNumber(update: Boolean = true) {
        if (currentCount == 0 || numbers.isEmpty()) {
            return
        }

        numbers.removeAt(numbers.size - 1)
        setCount(numbers.size)

        if (update) {
            doOnCodeUpdated?.invoke(code)
        }
    }

    fun setCount(count: Int) {
        if (currentCount == count) {
            return
        } else if (count == 0) {
            state = PinCodeState.DEFAULT
        }

        currentCount = count
        for (i in dots.indices) {
            if (count > i) {
                if (state == PinCodeState.ERROR) {
                    dots[i].setError()
                } else {
                    dots[i].setActive()
                }
            } else {
                dots[i].setDefault()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var offsetX = dotGap.toFloat()
        val offsetY = (height - dotSize) / 2f
        for (dot in dots) {
            canvas.withSave {
                translate(offsetX, offsetY)
                dot.draw(this)
            }
            offsetX += dotGap + dotSize
        }
    }

    fun setError() {
        if (state == PinCodeState.ERROR) {
            return
        }

        hapticReject()

        state = PinCodeState.ERROR

        for ((index, dot) in dots.withIndex()) {
            dot.setError()
            postDelayed({
                removeLastNumber(false)
            }, ((animationDuration / 2) * index) + animationDuration)
        }
    }

    fun clear() {
        numbers.clear()
        setCount(0)
    }

    fun setSuccess() {
        if (state == PinCodeState.SUCCESS) {
            return
        }

        hapticConfirm()

        state = PinCodeState.SUCCESS

        for (dot in dots) {
            dot.state = DotState.SUCCESS
        }

        val successAnimation = ValueAnimator.ofFloat(1f, dotActiveScale)
        successAnimation.duration = animationDuration * 2
        successAnimation.addUpdateListener {
            val scale = it.animatedValue as Float
            for (dot in dots) {
                dot.scale = scale
            }
        }
        successAnimation.interpolator = ReverseInterpolator()
        successAnimation.start()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val fullWidth = (dotGap + dotSize) * dots.size + dotGap
        super.onMeasure(MeasureSpec.makeMeasureSpec(fullWidth.toInt(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(16.dp, MeasureSpec.EXACTLY))
    }

    override fun hasOverlappingRendering() = false

    private inner class Dot {

        var state = DotState.DEFAULT
            set(value) {
                if (value != field) {
                    if (field == DotState.DEFAULT && value == DotState.ACTIVE) {
                        startScaleAnimation()
                    }

                    field = value
                    when (value) {
                        DotState.DEFAULT -> {
                            setColor(defaultColor)
                        }
                        DotState.ACTIVE -> {
                            setColor(activeColor)
                        }
                        DotState.ERROR -> {
                            setColor(errorColor)
                        }
                        DotState.SUCCESS -> {
                            setColor(doneColor)
                        }
                    }
                }
            }

        var scale = 1f
            set(value) {
                if (value != field) {
                    field = value
                    invalidate()
                }
            }

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = defaultColor
        }

        private val changeColorAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = animationDuration
        }

        private val scaleColorAnimator = ValueAnimator.ofFloat(1f, dotActiveScale).apply {
            duration = animationDuration * 2
            interpolator = ReverseInterpolator()
        }

        fun draw(canvas: Canvas) {
            if (scale != 1f) {
                canvas.scale(scale, scale, dotSize / 2, dotSize / 2)
            }
            canvas.drawOval(0f, 0f, dotSize, dotSize, paint)
        }

        fun setActive() {
            state = DotState.ACTIVE
        }

        fun setDefault() {
            state = DotState.DEFAULT
        }

        fun setError() {
            state = DotState.ERROR
        }

        private fun setColor(newColor: Int) {
            val oldColor = paint.color
            changeColorAnimator.addUpdateListener {
                val value = it.animatedValue as Float
                paint.color = ArgbEvaluator.instance.evaluate(value, oldColor, newColor)
                invalidate()
            }
            changeColorAnimator.start()
        }

        private fun startScaleAnimation() {
            scaleColorAnimator.addUpdateListener {
                scale = it.animatedValue as Float
            }
            scaleColorAnimator.start()
        }
    }
}