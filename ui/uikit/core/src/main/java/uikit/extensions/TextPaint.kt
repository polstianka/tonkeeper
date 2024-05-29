package uikit.extensions

import android.content.Context
import android.content.res.TypedArray
import android.text.TextPaint
import androidx.annotation.StyleRes
import androidx.appcompat.R
import androidx.core.content.res.ResourcesCompat

fun TextPaint.setTextAppearance(context: Context, @StyleRes textAppearanceRes: Int) {
    val a: TypedArray = context.obtainStyledAttributes(textAppearanceRes, R.styleable.TextAppearance)
    val typefaceRes = a.getInt(R.styleable.TextAppearance_android_typeface, 0)
    if (typefaceRes != 0) {
        val typeface = ResourcesCompat.getFont(context, typefaceRes)
        setTypeface(typeface)
    }
    val textSize = a.getDimension(R.styleable.TextAppearance_android_textSize, textSize)
    setTextSize(textSize)
    val textColor = a.getColor(R.styleable.TextAppearance_android_textColor, color)
    color = textColor

    a.recycle()
}
