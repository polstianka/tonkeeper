package com.tonapps.tonkeeper.fragment.staking.deposit.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.setPadding
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.accentGreenColor
import com.tonapps.uikit.list.ListCell
import uikit.drawable.CellBackgroundDrawable
import uikit.extensions.dp
import uikit.extensions.drawable


class PoolSelectedView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {
    val titleView: AppCompatTextView
    val descriptionView: AppCompatTextView
    val labelView: AppCompatTextView
    val iconView: AppCompatImageView

    init {
        inflate(context, R.layout.view_staking_pool, this)
        background = ListCell.Position.SINGLE.drawable(context, radius = 18f.dp)
        setPadding(16.dp)

        titleView = findViewById(R.id.title)
        descriptionView = findViewById(R.id.description)
        labelView = findViewById(R.id.label)
        labelView.background = CellBackgroundDrawable.create(
            context,
            ListCell.Position.SINGLE,
            (context.accentGreenColor and 0x29FFFFFF),
            4f.dp
        )

        iconView = findViewById(R.id.icon)
        iconView.clipToOutline = true
    }
}