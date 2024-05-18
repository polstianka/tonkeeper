package com.tonapps.tonkeeper.ui.component

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeperx.R
import uikit.widget.FrescoView

class AssetView(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    init {
        inflate(context, R.layout.view_asset, this)
    }

    private val iconView = findViewById<FrescoView>(R.id.icon)
    private val nameTextView = findViewById<AppCompatTextView>(R.id.asset_name_text)

    fun setData(
        name: String,
        uri: String,
    ) {
        nameTextView.text = name
        iconView.setImageURI(uri)
    }
}
