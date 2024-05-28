package com.tonapps.tonkeeper.fragment.swap.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.fragment.swap.model.TokenInfo
import com.tonapps.wallet.localization.Localization
import uikit.R
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.useAttributes
import uikit.widget.FrescoView
import uikit.widget.RowLayout
import com.tonapps.tonkeeperx.R as ProjectR

class TokenChipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RowLayout(context, attrs, defStyle) {

    private val iconView: FrescoView
    private val titleView: AppCompatTextView

    var token: TokenInfo? = null
        set(value) {
            field = value
            if (value == null) {
                iconView.visibility = GONE
                setPadding(
                    context.getDimensionPixelSize(R.dimen.tokenChipPadding),
                    context.getDimensionPixelSize(R.dimen.tokenChipPadding),
                    context.getDimensionPixelSize(R.dimen.gap),
                    context.getDimensionPixelSize(R.dimen.tokenChipPadding)
                )
                title = context.getString(Localization.choose)
            } else {
                setPadding(
                    context.getDimensionPixelSize(R.dimen.tokenChipPadding),
                    context.getDimensionPixelSize(R.dimen.tokenChipPadding),
                    context.getDimensionPixelSize(R.dimen.tokenChipRightPadding),
                    context.getDimensionPixelSize(R.dimen.tokenChipPadding)
                )
                iconView.visibility = VISIBLE
                iconView.setImageURI(value.iconUri)
                title = value.symbol
            }
        }

    var title: String = context.getString(Localization.choose)
        set(value) {
            field = value
            titleView.text = value
        }

    init {
        setPadding(
            context.getDimensionPixelSize(R.dimen.tokenChipRightPadding),
            context.getDimensionPixelSize(R.dimen.tokenChipPadding),
            context.getDimensionPixelSize(R.dimen.tokenChipRightPadding),
            context.getDimensionPixelSize(R.dimen.tokenChipPadding)
        )
        setBackgroundResource(R.drawable.bg_button_tertiary)
        inflate(context, ProjectR.layout.view_token_chip, this)

        iconView = findViewById(R.id.action_cell_icon)
        titleView = findViewById(R.id.action_cell_title)

        context.useAttributes(attrs, R.styleable.TokenChipView) {
            title = it.getString(R.styleable.TokenChipView_android_title) ?: context.getString(
                Localization.choose
            )

            val singleLine = it.getBoolean(R.styleable.TokenChipView_android_singleLine, false)
            if (singleLine) {
                titleView.setSingleLine()
            }
        }
    }
}