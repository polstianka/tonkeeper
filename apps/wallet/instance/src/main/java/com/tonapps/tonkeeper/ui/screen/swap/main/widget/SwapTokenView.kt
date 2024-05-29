package com.tonapps.tonkeeper.ui.screen.swap.main.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.facebook.common.util.UriUtil
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.iconSecondaryColor
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.localization.Localization
import uikit.extensions.drawable
import uikit.widget.FrescoView

class SwapTokenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ConstraintLayout(context, attrs, defStyle) {
    private val iconView: FrescoView
    private val textView: AppCompatTextView
    private val failureIcon: Drawable

    init {
        setBackgroundResource(uikit.R.drawable.bg_button_tertiary)

        inflate(context, R.layout.view_swap_token, this)

        failureIcon = context.drawable(R.drawable.ic_question_mark_16)
        failureIcon.setTint(context.iconSecondaryColor)

        iconView = findViewById(android.R.id.icon)
        textView = findViewById(android.R.id.text1)
    }

    var token: TokenEntity? = null
        set(value) {
            field = value
            if (value != null) {
                textView.text = value.symbol
                iconView.visibility = View.VISIBLE
                if (UriUtil.isLocalResourceUri(value.imageUri)) {
                    iconView.setFailureImage(null)
                } else {
                    iconView.setFailureImage(failureIcon)
                }
                iconView.setImageURI(value.imageUri, this)
            } else {
                textView.text = textView.resources.getString(Localization.swap_choose)
                iconView.visibility = View.GONE
            }
        }
}