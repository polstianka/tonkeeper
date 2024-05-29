package com.tonapps.tonkeeper.fragment.swap.view

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.accentGreenColor
import com.tonapps.uikit.color.backgroundContentTintColor
import com.tonapps.uikit.list.ListCell
import uikit.drawable.CellBackgroundDrawable
import uikit.extensions.dp
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.setPaddingHorizontal
import uikit.extensions.useAttributes
import uikit.navigation.Navigation.Companion.navigation

class SwapInfoRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyle) {

    private val titleView: AppCompatTextView
    private val valueView: AppCompatTextView
    private val infoView: AppCompatImageView
    val labelView: AppCompatTextView

    init {
        inflate(context, R.layout.view_swap_info_row, this)
        setPaddingHorizontal(context.getDimensionPixelSize(uikit.R.dimen.offsetMedium))

        orientation = HORIZONTAL

        titleView = findViewById(R.id.title)
        valueView = findViewById(R.id.value)
        infoView = findViewById(R.id.information)
        labelView = findViewById(R.id.label)
        labelView.background = CellBackgroundDrawable.create(
            context,
            ListCell.Position.SINGLE,
            (context.accentGreenColor and 0x29FFFFFF),
            4f.dp
        )

        context.useAttributes(attrs, R.styleable.SwapInfoRowView) {
            titleView.text = it.getString(R.styleable.SwapInfoRowView_android_title)

            val hint = it.getString(R.styleable.SwapInfoRowView_android_hint)
            hint?.isNotEmpty() ?: false

            infoView.isVisible = !hint.isNullOrEmpty()
            if (!hint.isNullOrEmpty()) {
                background = AppCompatResources.getDrawable(context, uikit.R.drawable.bg_ripple_3)

                /*background = CellBackgroundDrawable.create(
                    context,
                    position = ListCell.Position.SINGLE,
                    radius = 0f
                )*/
                setOnClickListener {
                    context.navigation?.toast(hint, false, context.backgroundContentTintColor)
                }
            }
        }
    }

    fun setTitle(value: String?) {
        titleView.text = value
    }

    fun setValue(value: String?) {
        valueView.text = value
    }

    fun setValueColorInt(@ColorInt color: Int) {
        valueView.setTextColor(color)
    }

    fun setTitleColorInt(@ColorInt color: Int) {
        titleView.setTextColor(color)
    }
}