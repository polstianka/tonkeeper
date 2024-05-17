package com.tonapps.tonkeeper.ui.component.swap

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.tonkeeperx.R
import uikit.extensions.dp
import uikit.extensions.setPaddingStart
import uikit.extensions.useAttributes

class TokenButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayoutCompat(context, attrs, defStyle) {

    private val icon: SimpleDraweeView
    private val label: TextView

    init {
        inflate(context, R.layout.view_token_button, this)

        setBackgroundResource(uikit.R.drawable.bg_button_tertiary)

        icon = findViewById(R.id.icon)
        label = findViewById(R.id.label)

        context.useAttributes(attrs, R.styleable.TokenButtonView) {
            it.getString(R.styleable.TokenButtonView_android_text)?.let { text ->
                label.text = text
            }
        }
    }

    fun setToken(url: Uri?, name: CharSequence) {
        icon.setImageURI(url, this)
        icon.isVisible = true
        label.setPaddingStart(4.dp)
        label.text = name
    }

    fun setButton(text: CharSequence) {
        icon.isGone = true
        label.setPaddingStart(16.dp)
        label.text = text
    }

}