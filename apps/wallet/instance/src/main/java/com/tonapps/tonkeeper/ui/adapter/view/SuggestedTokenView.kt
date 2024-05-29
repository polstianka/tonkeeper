package com.tonapps.tonkeeper.ui.adapter.view

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.setPadding
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.buttonTertiaryBackgroundColor
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.TokenEntity
import uikit.drawable.CellBackgroundDrawable
import uikit.extensions.dp
import uikit.widget.FrescoView
import uikit.widget.RowLayout

class SuggestedTokenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RowLayout(context, attrs, defStyle) {

    private val iconView: FrescoView
    private val titleView: AppCompatTextView

    init {
        inflate(context, R.layout.holder_token_suggested, this)
        gravity = Gravity.CENTER
        minimumHeight = 36.dp
        setPadding(4.dp)

        background = CellBackgroundDrawable.create(
            context,
            ListCell.Position.SINGLE,
            context.buttonTertiaryBackgroundColor,
            18f.dp
        )

        iconView = findViewById(R.id.icon)
        iconView.visibility = GONE
        titleView = findViewById(R.id.title)
        setToken(null, null)
    }

    fun setToken(token: TokenEntity?) {
        setToken(token?.symbol, token?.imageUri)
    }

    fun setToken(symbol: String?, uri: Uri?) {
        if (symbol.isNullOrEmpty()) {
            iconView.visibility = GONE
            titleView.text =
                context.getString(com.tonapps.wallet.localization.R.string.swap_choose_token_default)
        } else {
            titleView.text = symbol
            iconView.visibility = VISIBLE
            iconView.setImageURI(uri)
        }
    }
}