package com.tonapps.tonkeeper.fragment.staking.deposit.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.buttonSecondaryBackgroundColor
import com.tonapps.uikit.list.ListCell
import uikit.drawable.CellBackgroundDrawable
import uikit.extensions.dp
import uikit.extensions.setPaddingHorizontal


class LinkView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyle) {

    val titleView: AppCompatTextView
    val iconView: AppCompatImageView

    init {
        inflate(context, R.layout.view_cell_stake_link, this)
        background = CellBackgroundDrawable.create(
            context,
            ListCell.Position.SINGLE,
            context.buttonSecondaryBackgroundColor,
            18f.dp
        )

        setPaddingHorizontal(16.dp)
        orientation = HORIZONTAL
        gravity = Gravity.CENTER

        layoutParams?.apply {
            width = LayoutParams.WRAP_CONTENT
            height = 36.dp
        } ?: run {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, 36.dp)
        }

        iconView = findViewById(R.id.icon)
        titleView = findViewById(R.id.title)
    }
}