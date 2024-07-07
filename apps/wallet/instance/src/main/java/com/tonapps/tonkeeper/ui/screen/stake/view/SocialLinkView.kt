package com.tonapps.tonkeeper.ui.screen.stake.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.textPrimaryColor
import com.tonapps.wallet.localization.Localization
import uikit.extensions.dp
import uikit.extensions.setPaddingHorizontal
import uikit.extensions.setPaddingVertical

typealias TitleIconData = Pair<String, Int>

class SocialLinkView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyle) {

    private val textView: AppCompatTextView
    private val iconView: AppCompatImageView

    init {
        background = ContextCompat.getDrawable(context, R.drawable.bg_social_link)
        orientation = HORIZONTAL
        setPaddingHorizontal(16.dp)
        setPaddingVertical(8.dp)
        iconView = AppCompatImageView(context).apply {
            layoutParams = LayoutParams(16.dp, 16.dp).apply {
                setMargins(0, 0, 8.dp, 0)
                setGravity(Gravity.CENTER_VERTICAL)
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
        val uri = Uri.parse(link)
        val (title, iconRes) = getTitleAndIcon(uri.host ?: link)
        textView.text = title
        iconView.setImageResource(iconRes)
        setOnClickListener { openLink(uri) }
    }

    private fun openLink(link: Uri) {
        context.startActivity(Intent(Intent.ACTION_VIEW, link))
    }

    private fun getTitleAndIcon(host: String): TitleIconData {
        return when {
            host.contains("t.me") -> TitleIconData(
                context.getString(Localization.community),
                com.tonapps.uikit.icon.R.drawable.ic_telegram_16
            )

            host.contains("twitter.com") -> TitleIconData(
                context.getString(Localization.twitter),
                com.tonapps.uikit.icon.R.drawable.ic_twitter_16
            )

            else -> TitleIconData(host, com.tonapps.uikit.icon.R.drawable.ic_globe_16)
        }
    }
}