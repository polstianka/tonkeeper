package uikit.widget

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.tonapps.uikit.color.backgroundContentColor
import uikit.R
import uikit.extensions.cornerMedium
import uikit.extensions.dp
import uikit.extensions.round
import uikit.extensions.useAttributes

class DropdownButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyle) {

    private val icon: ImageView

    init {
        orientation = HORIZONTAL
        prepareBackground(context)

        val icon = ImageView(context)
        this.icon = icon
        prepareIcon(icon, attrs)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addView(icon)
    }

    private fun prepareIcon(
        icon: ImageView,
        attrs: AttributeSet?
    ) {
        icon.layoutParams = LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setVerticalGravity(Gravity.CENTER_VERTICAL)
            val marginSmall = 16f.dp.toInt()
            val marginBig = 22f.dp.toInt()
            leftMargin = marginSmall
            topMargin = marginSmall
            bottomMargin = marginSmall
            rightMargin = marginBig
        }
        context.useAttributes(attrs, R.styleable.DropdownButton) { typedArray ->
            if (typedArray.hasValue(R.styleable.DropdownButton_dropdownIcon)) {
                icon.setImageDrawable(
                    typedArray.getDrawable(R.styleable.DropdownButton_dropdownIcon)
                )
            }
            if (typedArray.hasValue(R.styleable.DropdownButton_dropdownIconTint)) {
                icon.imageTintList = typedArray.getColorStateList(
                    R.styleable.DropdownButton_dropdownIconTint
                )
            }
        }
    }

    private fun prepareBackground(context: Context) {
        val typedValue = TypedValue()
        val isResolved = context.theme.resolveAttribute(
            android.R.attr.selectableItemBackground,
            typedValue,
            true
        )
        if (isResolved) {
            val colorDrawable = ColorDrawable(context.backgroundContentColor)
            val selectableItemDrawable = ContextCompat.getDrawable(context, typedValue.resourceId)
            val layeredDrawable = LayerDrawable(arrayOf(colorDrawable, selectableItemDrawable))
            background = layeredDrawable
        } else {
            setBackgroundColor(context.backgroundContentColor)
        }
        round(context.cornerMedium)
    }
}