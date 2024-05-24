package com.tonapps.qr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.Encoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.math.roundToInt

object QR {

    val scope = CoroutineScope(Dispatchers.Main)

    class Builder(private val content: String) {
        private var fillColor = Color.BLACK
        private var backgroundColor = Color.WHITE
        private var withCutout = false
        private var cornerSquareSize = 0
        private var cutoutFirstBlock = 0
        private var cutoutBlockCount = 0
        private var size = 100
        private var errorCorrectionLevel = ErrorCorrectionLevel.M

        fun setSize(size: Int): Builder {
            this.size = size
            return this
        }

        fun setErrorCorrectionLevel(errorCorrectionLevel: ErrorCorrectionLevel): Builder {
            this.errorCorrectionLevel = errorCorrectionLevel
            return this
        }

        fun setWithCutout(withCutout: Boolean): Builder {
            this.withCutout = withCutout
            return this
        }

        fun setColor(backgroundColor: Int): Builder {
            this.backgroundColor = backgroundColor
            return this
        }

        fun setFillColor(fillColor: Int): Builder {
            this.fillColor = fillColor
            return this
        }

        fun build(context: Context): BitmapDrawable {
            val bitmap = build()
            return BitmapDrawable(context.resources, bitmap)
        }

        fun build(): Bitmap {
            val hints = HashMap<EncodeHintType, Any?>()
            hints[EncodeHintType.MARGIN] = 0

            val qrCode = Encoder.encode(content, errorCorrectionLevel, hints)
            val matrix = qrCode.matrix

            val qrWidth = matrix.width
            val qrHeight = matrix.height
            val outputWidth = size.coerceAtLeast(qrWidth)
            val outputHeight = size.coerceAtLeast(qrHeight)
            val multiple = (outputWidth / qrWidth).coerceAtMost(outputHeight / qrHeight)
            val horizontalPadding = (outputWidth - matrix.width * multiple) / 2
            val verticalPadding = (outputHeight - matrix.height * multiple) / 2
            val size = multiple * matrix.width + horizontalPadding * 2

            if (withCutout) {
                cutoutBlockCount = ((size - 32) * 0.25f / multiple).roundToInt()
                if (cutoutBlockCount % 2 != matrix.width % 2) {
                    ++cutoutBlockCount
                }
                cutoutFirstBlock = (matrix.width - cutoutBlockCount) / 2
            }

            for (x in 0 until matrix.width) {
                if (hasPoint(matrix, x, 0)) {
                    cornerSquareSize++
                } else {
                    break
                }
            }

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val fillPaint = Paint()
            fillPaint.color = fillColor
            for (i in 0..2) {
                var x: Int
                var y: Int
                when (i) {
                    0 -> {
                        x = horizontalPadding
                        y = verticalPadding
                    }
                    1 -> {
                        x = bitmap.width - cornerSquareSize * multiple - horizontalPadding
                        y = verticalPadding
                    }
                    else -> {
                        x = horizontalPadding
                        y = bitmap.height - cornerSquareSize * multiple - verticalPadding
                    }
                }
                fillPaint.color = fillColor
                canvas.drawRect(
                    x.toFloat(),
                    y.toFloat(),
                    (x + cornerSquareSize * multiple).toFloat(),
                    (y + cornerSquareSize * multiple).toFloat(),
                    fillPaint
                )
                fillPaint.color = backgroundColor
                canvas.drawRect(
                    (x + multiple).toFloat(),
                    (y + multiple).toFloat(),
                    (x + (cornerSquareSize - 1) * multiple).toFloat(),
                    (y + (cornerSquareSize - 1) * multiple).toFloat(),
                    fillPaint
                )
                fillPaint.color = fillColor
                canvas.drawRect(
                    (x + multiple * 2).toFloat(),
                    (y + multiple * 2).toFloat(),
                    (x + (cornerSquareSize - 2) * multiple).toFloat(),
                    (y + (cornerSquareSize - 2) * multiple).toFloat(),
                    fillPaint
                )
            }
            var y = 0
            var yOutput = verticalPadding
            while (y < matrix.height) {
                var x = 0
                var xOutput = horizontalPadding
                while (x < matrix.width) {
                    if (hasPoint(matrix, x, y)) {
                        fillPaint.color = fillColor
                        canvas.drawRect(
                            xOutput.toFloat(),
                            yOutput.toFloat(),
                            (xOutput + multiple).toFloat(),
                            (yOutput + multiple).toFloat(),
                            fillPaint
                        )
                    }
                    ++x
                    xOutput += multiple
                }
                ++y
                yOutput += multiple
            }
            canvas.setBitmap(null)
            val newBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                bitmap.copy(Bitmap.Config.HARDWARE, false)
            } else {
                bitmap.copy(Bitmap.Config.RGB_565, false)
            }
            bitmap.recycle()
            return newBitmap
        }

        private fun hasPoint(matrix: ByteMatrix, x: Int, y: Int): Boolean {
            if (cutoutFirstBlock <= x && x < cutoutFirstBlock + cutoutBlockCount && cutoutFirstBlock <= y && y < cutoutFirstBlock + cutoutBlockCount) {
                return false
            }
            if (x < cornerSquareSize && y < cornerSquareSize) {
                return false
            }
            if (matrix.width - cornerSquareSize <= x && y < cornerSquareSize) {
                return false
            }
            return if (x < cornerSquareSize && matrix.height - cornerSquareSize <= y) {
                false
            } else 0 <= x && x < matrix.width && 0 <= y && y < matrix.height && matrix[x, y].toInt() == 1
        }
    }


}