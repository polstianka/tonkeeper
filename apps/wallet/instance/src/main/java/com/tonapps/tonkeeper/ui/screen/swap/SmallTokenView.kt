package com.tonapps.tonkeeper.ui.screen.swap

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.facebook.drawee.generic.RoundingParams
import com.tonapps.tonkeeperx.R
import uikit.extensions.dp
import uikit.widget.FrescoView
import uikit.widget.RowLayout

class SmallTokenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RowLayout(context, attrs, defStyle) {

    private val icon: FrescoView
    private val symbol: TextView

    private var asset: AssetModel? = null

    init {
        gravity = Gravity.CENTER
        setBackgroundResource(R.drawable.bg_token_small_button)
        setPadding(4.dp)

        icon = FrescoView(context).apply {
            layoutParams = LayoutParams(28.dp, 28.dp)
            hierarchy.roundingParams = RoundingParams.asCircle()
        }
        symbol = TextView(context).apply {
            setSingleLine()
            setTextAppearance(uikit.R.style.TextAppearance_Label1)
            setTextColor(
                ContextCompat.getColor(
                    context,
                    com.tonapps.uikit.color.R.color.buttonSecondaryForegroundDark
                )
            )

            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            setPadding(6.dp, 0.dp, 10.dp, 0.dp)
        }
        addView(icon)
        addView(symbol)
    }

    fun setAsset(assetModel: AssetModel?) {
        this.asset = assetModel
        if (assetModel != null) {
            setText(assetModel.token.symbol)
            setIcon(assetModel.token.imageUri)
            setIconVisibility(true)
        } else {
            setIconVisibility(false)
            setText(context.resources.getString(com.tonapps.wallet.localization.R.string.choose))
        }
    }

    fun setText(symbol: String) {
        this.symbol.text = symbol
    }

    fun setIcon(uri: Uri) {
        icon.setImageURI(uri)
    }

    fun setIconVisibility(visible: Boolean) {
        icon.isVisible = visible
        if (visible) {
            symbol.setPadding(6.dp, 0.dp, 10.dp, 0.dp)
        } else {
            symbol.setPadding(10.dp, 6.dp, 10.dp, 6.dp)
        }
    }

    fun setOnAssetClickListener(click: (AssetModel) -> Unit) {
        super.setOnClickListener { asset?.let { it1 -> click(it1) } }
    }

}