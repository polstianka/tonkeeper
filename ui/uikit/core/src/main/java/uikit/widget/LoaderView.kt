package uikit.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import com.tonapps.uikit.color.iconSecondaryColor
import uikit.R
import uikit.extensions.dp
import uikit.extensions.isVisibleForUser
import uikit.extensions.useAttributes

class LoaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle) {

    var type = TYPE_DEFAULT
    private val paint: Paint
    private val trackPaint: Paint
    private var size = 0f
    private var progress = 0.4f
    private val bounds: RectF
    private var indeterminateSweep = 0f
    private var indeterminateRotateOffset = 0f
    private var thickness: Float
    private var color = 0
    private var trackColor = Color.TRANSPARENT
    private val animDuration: Int
    private val animSteps: Int
    private var startAngle: Float
    private var startAngleRotate: ValueAnimator? = null
    private var progressAnimator: ValueAnimator? = null
    private var indeterminateAnimator: AnimatorSet? = null
    private var animator: ObjectAnimator? = null

    init {
        thickness = 1.5f.dp
        startAngle = -90f
        animDuration = 4000
        animSteps = 3
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bounds = RectF()
        updatePaint()

        context.useAttributes(attrs, R.styleable.LoaderView) {
            val color = it.getColor(R.styleable.LoaderView_android_color, context.iconSecondaryColor)
            setColor(color)

            val trackColor = it.getColor(R.styleable.LoaderView_android_trackTint, Color.TRANSPARENT)
            setTrackColor(trackColor)
        }
    }

    fun setProgressType(type: Int) {
        if (this.type == type) {
            return
        }
        this.type = type
        updatePaint()
        stopAnimation()
        startAnimation()
    }

    fun setProgressAnimation(progress: Float) {
        if (animator != null) {
            animator!!.cancel()
        }
        animator = ObjectAnimator.ofFloat(this, "progress", this.progress, progress)
        animator?.duration = 320
        animator?.start()
    }

    fun getProgress(): Float {
        return progress
    }

    fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }

    fun setSize(size: Float) {
        thickness = size
        updatePaint()
    }

    fun setColor(color: Int) {
        this.color = color
        updatePaint()
    }

    fun setTrackColor(color: Int) {
        this.trackColor = color
        updatePaint()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val xPad = paddingLeft + paddingRight
        val yPad = paddingTop + paddingBottom
        val width = measuredWidth - xPad
        val height = measuredHeight - yPad
        size = width.coerceAtMost(height).toFloat()
        setMeasuredDimension((size + xPad).toInt(), (size + yPad).toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        size = w.coerceAtMost(h).toFloat()
        updateBounds()
    }

    private fun updateBounds() {
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        bounds.set(
            (paddingLeft + thickness),
            (paddingTop + thickness),
            (size - paddingLeft - thickness),
            (size - paddingTop - thickness)
        )
    }

    private fun updatePaint() {
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = thickness
        paint.strokeCap = Paint.Cap.ROUND

        trackPaint.color = trackColor
        trackPaint.style = Paint.Style.STROKE
        trackPaint.strokeWidth = thickness
        trackPaint.strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (trackColor != Color.TRANSPARENT) {
            canvas.drawArc(bounds, 0f, 360f, false, trackPaint)
        }
        when (type) {
            TYPE_DEFAULT -> drawDefault(canvas)
            TYPE_PROGRESS -> drawProgress(canvas)
        }
    }

    private fun drawDefault(canvas: Canvas) {
        canvas.drawArc(bounds, startAngle + indeterminateRotateOffset, indeterminateSweep, false, paint)
    }

    private fun drawProgress(canvas: Canvas) {
        val angle = 360 * progress
        canvas.drawArc(bounds, startAngle + indeterminateRotateOffset, angle, false, paint)
    }

    private fun cancelAnimator(animator: Animator?) {
        if (animator != null && animator.isRunning) {
            animator.cancel()
        }
    }

    private fun cancelAnimators(vararg animator: Animator?) {
        for (a in animator) {
            cancelAnimator(a)
        }
    }

    private fun startProgressAnimation() {
        val rotateAnimator = ValueAnimator.ofFloat(0f, 360f)
        rotateAnimator.duration = 2000
        rotateAnimator.repeatCount = ValueAnimator.INFINITE
        rotateAnimator.interpolator = LinearInterpolator()
        rotateAnimator.addUpdateListener { animation: ValueAnimator ->
            indeterminateRotateOffset = animation.animatedValue as Float
            invalidate()
        }
        rotateAnimator.start()
    }

    private fun startDefaultAnimation() {
        stopAnimation()
        indeterminateSweep = INDETERMINANT_MIN_SWEEP
        indeterminateAnimator = AnimatorSet()
        var prevSet: AnimatorSet? = null
        var nextSet: AnimatorSet
        for (k in 0 until animSteps) {
            nextSet = createIndeterminateAnimator(k.toFloat())
            val builder = indeterminateAnimator!!.play(nextSet)
            if (prevSet != null) {
                builder.after(prevSet)
            }
            prevSet = nextSet
        }
        indeterminateAnimator!!.addListener(object : AnimatorListenerAdapter() {
            var wasCancelled = false
            override fun onAnimationCancel(animation: Animator) {
                wasCancelled = true
            }

            override fun onAnimationEnd(animation: Animator) {
                if (!wasCancelled) {
                    startAnimation()
                }
            }
        })
        indeterminateAnimator!!.start()
    }

    fun startAnimation() {
        if (!isVisibleForUser) {
            return
        }

        if (type == TYPE_PROGRESS) {
            startProgressAnimation()
        } else {
            startDefaultAnimation()
        }
    }

    fun stopAnimation() {
        cancelAnimators(startAngleRotate, progressAnimator, indeterminateAnimator)
    }

    private fun createIndeterminateAnimator(step: Float): AnimatorSet {
        val maxSweep = 360f * (animSteps - 1) / animSteps + INDETERMINANT_MIN_SWEEP
        val start = -90f + step * (maxSweep - INDETERMINANT_MIN_SWEEP)
        val frontEndExtend = ValueAnimator.ofFloat(INDETERMINANT_MIN_SWEEP, maxSweep)
        frontEndExtend.duration = (animDuration / animSteps / 2).toLong()
        frontEndExtend.interpolator = DecelerateInterpolator(1f)
        frontEndExtend.addUpdateListener { animation: ValueAnimator ->
            indeterminateSweep = animation.animatedValue as Float
            invalidate()
        }
        val rotateAnimator1 =
            ValueAnimator.ofFloat(step * 720f / animSteps, (step + .5f) * 720f / animSteps)
        rotateAnimator1.duration = (animDuration / animSteps / 2).toLong()
        rotateAnimator1.interpolator = LinearInterpolator()
        rotateAnimator1.addUpdateListener { animation: ValueAnimator ->
            indeterminateRotateOffset = animation.animatedValue as Float
        }
        val backEndRetract =
            ValueAnimator.ofFloat(start, start + maxSweep - INDETERMINANT_MIN_SWEEP)
        backEndRetract.duration = (animDuration / animSteps / 2).toLong()
        backEndRetract.interpolator = DecelerateInterpolator(1f)
        backEndRetract.addUpdateListener { animation: ValueAnimator ->
            startAngle = animation.animatedValue as Float
            indeterminateSweep = maxSweep - startAngle + start
            invalidate()
        }
        val rotateAnimator2 =
            ValueAnimator.ofFloat((step + .5f) * 720f / animSteps, (step + 1) * 720f / animSteps)
        rotateAnimator2.duration = (animDuration / animSteps / 2).toLong()
        rotateAnimator2.interpolator = LinearInterpolator()
        rotateAnimator2.addUpdateListener { animation: ValueAnimator ->
            indeterminateRotateOffset = animation.animatedValue as Float
        }
        val set = AnimatorSet()
        set.play(frontEndExtend).with(rotateAnimator1)
        set.play(backEndRetract).with(rotateAnimator2).after(rotateAnimator1)
        return set
    }

    /*override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }*/

    private var visibleOld: Boolean = false

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (visibleOld != isVisible) {
            visibleOld = isVisible
            if (isVisible) {
                startAnimation()
            } else {
                stopAnimation()
            }
        }
    }

    override fun hasOverlappingRendering() = false

    companion object {
        private const val INDETERMINANT_MIN_SWEEP = 15f
        const val TYPE_PROGRESS = 1
        const val TYPE_DEFAULT = 2
    }
}