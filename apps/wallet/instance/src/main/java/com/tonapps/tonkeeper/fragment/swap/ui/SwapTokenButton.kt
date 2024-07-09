package com.tonapps.tonkeeper.fragment.swap.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import coil.transform.RoundedCornersTransformation
import com.tonapps.tonkeeper.core.loadUri
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetBalance
import com.tonapps.tonkeeperx.R
import uikit.extensions.dp
import uikit.widget.RowLayout
import com.tonapps.wallet.localization.R as LocalizationR

class SwapTokenButton
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RowLayout(context, attrs, defStyle) {

    private val iconView: ImageView?
        get() = findViewById(R.id.view_swap_token_icon)
    private val textView: TextView?
        get() = findViewById(R.id.view_swap_token_text)
    var asset: DexAssetBalance? = null
        get() = field
        set(value) {
            field = value
            updateState(value)
        }

    init {
        inflate(context, R.layout.view_swap_token, this)
        background = ContextCompat.getDrawable(context, uikit.R.drawable.bg_button_tertiary)
        minimumHeight = 36f.dp.toInt()
        val padding = 4f.dp.toInt()
        setPadding(padding, padding, padding, padding)
    }

    private fun updateState(asset: DexAssetBalance?) {
        iconView?.isVisible = asset != null
        asset?.imageUri?.let { iconView?.loadUri(it, RoundedCornersTransformation(14f.dp)) }
        val text = asset?.symbol ?: context.getString(LocalizationR.string.choose)
        textView?.text = text
    }
}