package com.tonapps.tonkeeper.fragment.stake.ui

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.tonapps.tonkeeper.core.toString
import com.tonapps.tonkeeper.fragment.stake.pool_details.presentation.LinksChipModel
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.buttonSecondaryBackgroundColor
import com.tonapps.uikit.color.iconPrimaryColor
import com.tonapps.uikit.color.stateList
import com.tonapps.uikit.color.textPrimaryColor
import uikit.extensions.dp
import uikit.extensions.setThrottleClickListener
import uikit.widget.ColumnLayout

class PoolLinksView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ColumnLayout(context, attrs, defStyle)  {

    private var onChipClicked: ((LinksChipModel) -> Unit)? = null
    private val chipGroup: ChipGroup?
        get() = findViewById(R.id.view_pool_links_chip_group)

    init {
        inflate(context, R.layout.view_pool_links, this)
    }

    fun setOnChipClicked(action: (LinksChipModel) -> Unit) {
        onChipClicked = action
    }

    fun applyChips(chips: List<LinksChipModel>) {
        chipGroup?.removeAllViews()
        chips.forEach { model ->
            val chip = createChip(model)
            chipGroup?.addView(chip)
        }
    }

    private fun createChip(model: LinksChipModel): Chip {
        val chip = Chip(context)
        val horizontalPadding = 12f.dp

        chip.chipIcon = ContextCompat.getDrawable(context, model.iconResId)
        chip.chipIconTint = context.iconPrimaryColor.stateList
        chip.chipIconSize = 16f.dp
        chip.iconStartPadding = horizontalPadding

        chip.text = context.toString(model.text)
        chip.setTextColor(context.textPrimaryColor)
        chip.setTextAppearance(context, uikit.R.style.TextAppearance_Label2)
        chip.textEndPadding = horizontalPadding

        chip.setThrottleClickListener { onChipClicked?.invoke(model) }
        chip.chipBackgroundColor = context.buttonSecondaryBackgroundColor.stateList
        chip.chipMinHeight = 36f.dp
        return chip
    }

}