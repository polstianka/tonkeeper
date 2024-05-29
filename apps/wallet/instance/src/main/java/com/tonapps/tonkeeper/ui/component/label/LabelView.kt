package com.tonapps.tonkeeper.ui.component.label

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.accentBlueColor
import com.tonapps.uikit.list.ListCell
import uikit.drawable.CellBackgroundDrawable
import uikit.extensions.dp
import uikit.extensions.useAttributes
import kotlin.math.roundToInt

class LabelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {
    init {
        setTextAppearance(uikit.R.style.TextAppearance_Body4CAPS)
        setPadding(5.dp, 2.5f.dp.roundToInt(), 5.dp, 3.5f.dp.roundToInt())

        context.useAttributes(attrs, R.styleable.LabelView) {
            val color = it.getColor(R.styleable.LabelView_android_textColor, context.accentBlueColor)
            setTextColor(color)
            background = CellBackgroundDrawable.create(
                context,
                ListCell.Position.SINGLE,
                (color and 0x29FFFFFF),
                4f.dp
            )
        }
    }
}