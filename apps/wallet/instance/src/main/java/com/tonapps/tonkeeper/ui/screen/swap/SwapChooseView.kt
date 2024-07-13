package com.tonapps.tonkeeper.ui.screen.swap

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import uikit.extensions.dp
import uikit.widget.FrescoView
import uikit.widget.RowLayout

class SwapChooseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    suggest: Boolean = false
) : RowLayout(context, attrs, defStyle) {
    private var chooseTextView: AppCompatTextView
    private var iconView: FrescoView
    private val emptyText = Localization.choose

    init {
        inflate(
            context,
            if (suggest) R.layout.view_swap_choose_suggested else R.layout.view_swap_choose,
            this
        )
        chooseTextView = findViewById(R.id.choose_text)
        iconView = findViewById(R.id.token_icon)
    }

    fun setData(swapItem: SwapItem) {
        when (swapItem) {
            is SwapItem.Asset -> {
                val chooseTextViewParams = chooseTextView.layoutParams as MarginLayoutParams
                chooseTextViewParams.marginStart = 8.dp
                chooseTextViewParams.marginEnd = 16.dp
                iconView.visibility = VISIBLE
                iconView.setImageURI(swapItem.iconUrl)
                chooseTextView.text = swapItem.title
            }

            is SwapItem.Hint -> {
                chooseTextView.setText(emptyText)
                iconView.visibility = GONE
                val chooseTextViewParams = chooseTextView.layoutParams as MarginLayoutParams
                chooseTextViewParams.marginStart = 14.dp
                chooseTextViewParams.marginEnd = 14.dp
                chooseTextView.layoutParams = chooseTextViewParams
            }
        }
    }

    sealed class SwapItem {

        data class Hint(
            val stringRes: Int = Localization.choose
        ) : SwapItem()

        data class Asset(
            val title: String,
            val iconUrl: String
        ) : SwapItem()
    }
}