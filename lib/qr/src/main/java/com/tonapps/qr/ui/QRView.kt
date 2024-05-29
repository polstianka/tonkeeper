package com.tonapps.qr.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.graphics.withSave
import androidx.core.view.doOnLayout
import com.tonapps.qr.QR
import com.tonapps.qr.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QRView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle) {

    private companion object {
        private const val ALPHA = 255
        private const val FADE_DURATION = 285L
        private val interpolator = DecelerateInterpolator()
    }

    private val qrDrawable = QRDrawable(context)

    private var color: Int
        get() = qrDrawable.color
        set(value) {
            qrDrawable.color = value
        }

    private var withCutout: Boolean
        get() = qrDrawable.withCutout
        set(value) {
            qrDrawable.withCutout = value
        }

    private val size: Int
        get() = width - (paddingLeft + paddingRight)


    init {
        qrDrawable.callback = this
        context.theme.obtainStyledAttributes(attrs, R.styleable.QRView, 0, 0).apply {
            try {
                color = getColor(R.styleable.QRView_android_color, Color.WHITE)
                withCutout = getBoolean(R.styleable.QRView_with_cutout, false)
            } finally {
                recycle()
            }
        }
    }

    fun setContent(uri: Uri) {
        setContent(uri.toString())
    }

    fun setContent(content: String) {
        doOnLayout { qrDrawable.setData(size, content) }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.withSave {
            val translate = (width - size) / 2
            canvas.translate(translate.toFloat(), translate.toFloat())
            qrDrawable.draw(canvas)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        qrDrawable.setBounds(0, 0, size, size)
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who == qrDrawable
    }

    override fun hasOverlappingRendering() = false

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        qrDrawable.onDetach()
    }

    override fun onStartTemporaryDetach() {
        super.onStartTemporaryDetach()
        qrDrawable.onDetach()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        qrDrawable.onAttach()
    }

    override fun onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach()
        qrDrawable.onAttach()
    }
}