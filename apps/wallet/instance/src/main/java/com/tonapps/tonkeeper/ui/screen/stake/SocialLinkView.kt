package com.tonapps.tonkeeper.ui.screen.stake

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.textPrimaryColor
import uikit.extensions.dp
import uikit.extensions.setPaddingHorizontal
import uikit.extensions.setPaddingVertical
import com.tonapps.uikit.icon.R as IconR

typealias TitleIconData = Pair<String, Int>

class SocialLinkView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayoutCompat(context, attrs, defStyle) {

    private val textView: AppCompatTextView
    private val iconView: AppCompatImageView

    init {
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            setMargins(8.dp, 8.dp, 0, 0)
        }
        background = ContextCompat.getDrawable(context, R.drawable.bg_social_link)
        orientation = HORIZONTAL
        setPaddingHorizontal(16.dp)
        setPaddingVertical(8.dp)
        iconView = AppCompatImageView(context).apply {
            layoutParams = LayoutParams(16.dp, 16.dp).apply {
                setMargins(0, 0, 8.dp, 0)
            }
        }
        textView = AppCompatTextView(context).apply {
            setTextAppearance(uikit.R.style.TextAppearance_Label2)
            setTextColor(context.textPrimaryColor)
            setSingleLine()
        }
        addView(iconView)
        addView(textView)
    }

    fun setLink(link: String) {
        val (title, iconRes) = getTitleAndIcon(link)
        textView.text = title
        iconView.setImageResource(iconRes)
    }

    private fun getTitleAndIcon(link: String): TitleIconData {
        return when {
            link.contains("t.me/") -> TitleIconData("Community", IconR.drawable.ic_telegram_16)

            link.contains("twitter.com/") -> TitleIconData("Twitter", IconR.drawable.ic_twitter_16)

            else -> TitleIconData(link, IconR.drawable.ic_globe_16)
        }
    }
}