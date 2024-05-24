package uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import uikit.R
import uikit.extensions.applySelectableBgContent
import uikit.extensions.dp
import uikit.extensions.useAttributes

class DropdownButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyle) {

    private val icon: ImageView

    init {
        orientation = HORIZONTAL
        applySelectableBgContent()

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
}