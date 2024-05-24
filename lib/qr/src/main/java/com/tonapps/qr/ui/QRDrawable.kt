package com.tonapps.qr.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.tonapps.qr.QR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QRDrawable(
    private val context: Context
): Drawable() {

    private companion object {
        private const val CHUNK_SIZE = 256
        private const val NEXT_DELAY = 185L
    }

    private var currentAlpha = 255
    private var animationJob: Job? = null
    private var drawables = listOf<BitmapDrawable>()
    private var currentIndex = -1
    private val currentBitmapDrawable: BitmapDrawable?
        get() = drawables.getOrNull(currentIndex)

    var animation: Boolean = true
    var errorCorrectionLevel = ErrorCorrectionLevel.M

    val isEmpty: Boolean
        get() = drawables.isEmpty()

    var color: Int = Color.WHITE
        set(value) {
            if (field != value) {
                field = value
                invalidateSelf()
            }
        }

    var withCutout: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                invalidateSelf()
            }
        }

    private var isAttached = false
    private var data: Data? = null

    override fun draw(canvas: Canvas) {
        currentBitmapDrawable?.draw(canvas)
    }

    fun setData(size: Int, content: String) {
        clear()
        data = Data(size, content, animation)
        drawData()
    }

    private fun drawData() {
        if (!isAttached || !isEmpty) {
            return
        }

        val data = data ?: return
        currentAlpha = 255
        QR.scope.launch(Dispatchers.IO) {
            val drawables = mutableListOf<BitmapDrawable>()
            for (chunk in data.chunks) {
                drawables.add(createBitmapDrawable(data.size, chunk))
            }
            setDrawables(drawables.toList())
        }
    }

    private fun next() {
        if (!isEmpty) {
            currentIndex = (currentIndex + 1) % drawables.size
            invalidateSelf()
        }
    }

    private suspend fun setDrawables(
        newDrawables: List<BitmapDrawable>
    ) = withContext(Dispatchers.Main) {
        newDrawables.forEach { it.bounds = bounds }
        drawables = newDrawables
        currentIndex = -1
        next()
    }

    private fun createBitmapDrawable(size: Int, content: String): BitmapDrawable {
        val builder = QR.Builder(content)
        builder.setSize(size)
        builder.setColor(color)
        builder.setErrorCorrectionLevel(errorCorrectionLevel)
        builder.setWithCutout(withCutout)
        val drawable = builder.build(context)
        drawable.bounds = bounds
        return drawable
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        drawables.forEach { it.bounds = bounds }
    }

    override fun setAlpha(alpha: Int) {
        currentAlpha = alpha
        drawables.forEach { it.alpha = alpha }
    }

    override fun getAlpha() = currentAlpha

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    fun onDetach() {
        isAttached = false
        animationJob?.cancel()
        clear()
    }

    fun onAttach() {
        isAttached = true
        animationJob?.cancel()
        animationJob = QR.scope.launch {
            while (true) {
                delay(NEXT_DELAY)
                next()
            }
        }
        drawData()
    }

    private fun clear() {
        drawables.forEach { it.bitmap?.recycle() }
        drawables = emptyList()
        currentIndex = -1
    }

    private data class Data(
        val size: Int,
        val content: String,
        val animation: Boolean
    ) {

        val chunks: List<String> by lazy {
            if (animation) {
                content.chunked(CHUNK_SIZE)
            } else {
                listOf(content)
            }
        }
    }
}