package uikit.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.uikit.list.ListCell
import uikit.R
import uikit.drawable.InputDrawable
import uikit.extensions.drawable
import uikit.extensions.getDimensionPixelSize

class SelectableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : AppCompatTextView(context, attrs, defStyle) {
    private val inputDrawable = InputDrawable(context).apply { needBackground = false }

    init {

        foreground = inputDrawable
        background = ListCell.Position.SINGLE.drawable(context)
        minimumHeight = context.getDimensionPixelSize(R.dimen.barHeight)
    }

    var active: Boolean
        get() = inputDrawable.active
        set(value) {
            inputDrawable.active = value
        }

    fun forceActive(value: Boolean) = inputDrawable.forceActive(value)

    var error: Boolean
        get() = inputDrawable.error
        set(value) {
            inputDrawable.error = value
        }
}