package com.tonapps.tonkeeper.ui.screen.stake.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.tonapps.uikit.color.accentGreenColor
import uikit.extensions.dp
import uikit.extensions.setPaddingHorizontal
import uikit.extensions.setPaddingVertical

class BadgeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    init {
        background = ContextCompat.getDrawable(context, uikit.R.drawable.bg_badge_green)
        setPaddingVertical(3.dp)
        setPaddingHorizontal(5.dp)
        setTextAppearance(uikit.R.style.TextAppearance_Body4CAPS)
        setTextColor(context.accentGreenColor)
    }
}