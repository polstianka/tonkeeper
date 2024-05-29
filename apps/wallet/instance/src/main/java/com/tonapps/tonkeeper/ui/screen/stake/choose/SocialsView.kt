package com.tonapps.tonkeeper.ui.screen.stake.choose

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeperx.R
import uikit.widget.FrescoView
import uikit.widget.RowLayout
import java.net.URI

class SocialsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RowLayout(context, attrs, defStyle) {

    private var chooseTextView: AppCompatTextView
    private var iconView: FrescoView

    init {
        inflate(context, R.layout.view_social, this)
        chooseTextView = findViewById(R.id.text)
        iconView = findViewById(R.id.icon)
    }

    fun setData(item: Social) {
        chooseTextView.setText(item.title)
        iconView.setImageResource(item.icon)
    }

    data class Social(
        val title: String,
        val icon: Int
    ) {
        companion object {
            fun fromString(url: String): Social {
                if (url.contains("t.me/")) {
                    return Social("Community", R.drawable.ic_telegram_28)
                } else if (url.contains("https://tonstakers")) {
                    return Social("tonstakers.com", R.drawable.ic_globe_16)
                } else if (url.contains("twitter")) {
                    return Social("Twitter", R.drawable.twitter)
                } else if (url.contains("tonviewer")) {
                    return Social("tonviewer.com", R.drawable.magn_glass)
                }
                return try {
                    Social(
                        URI(url).host.removePrefix("www."),
                        R.drawable.ic_globe_16
                    )
                } catch (e: Exception) { Social(
                    url,
                    R.drawable.ic_globe_16
                )
                }
            }
        }
    }
}