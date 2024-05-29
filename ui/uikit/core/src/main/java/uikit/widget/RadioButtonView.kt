package uikit.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.tonapps.uikit.color.backgroundContentTintColor
import com.tonapps.uikit.color.buttonPrimaryBackgroundColor
import com.tonapps.uikit.icon.UIKitIcon
import uikit.extensions.dp
import uikit.extensions.getDrawable

class RadioButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle) {

    private companion object {
        private val size = 28.dp
        private val radius = 11f.dp
        private val dotRadius = 6f.dp
        private val strokeSize = 2f.dp
    }

    var doOnCheckedChanged: ((Boolean) -> Unit)? = null
        set(value) {
            field = value
            setOnClickListener {
                toggle()
            }
        }

    private val defaultPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.backgroundContentTintColor
        style = Paint.Style.STROKE
        strokeWidth = strokeSize
    }

    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.buttonPrimaryBackgroundColor
        style = Paint.Style.STROKE
        strokeWidth = strokeSize
    }

    private val activeDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.buttonPrimaryBackgroundColor
    }

    var checked: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                doOnCheckedChanged?.invoke(value)
                invalidate()
            }
        }

    fun toggle() {
        checked = !checked
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (checked) {
            canvas.drawCircle(
                size / 2f,
                size / 2f,
                radius,
                activePaint
            )
            canvas.drawCircle(
                size / 2f,
                size / 2f,
                dotRadius,
                activeDotPaint
            )
        } else {
            canvas.drawCircle(
                size / 2f,
                size / 2f,
                radius,
                defaultPaint
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        super.onMeasure(size, size)
    }

    override fun hasOverlappingRendering() = false
}