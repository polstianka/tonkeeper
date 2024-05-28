package uikit.widget.item

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.setPadding
import com.tonapps.uikit.list.ListCell
import uikit.R
import uikit.extensions.drawable
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.setPaddingHorizontal

open class BaseItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyle) {

    var position: ListCell.Position = com.tonapps.uikit.list.ListCell.Position.SINGLE
        set(value) {
            field = value
            background = value.drawable(context)
        }

    var multiLine: Boolean = false
        set(value) {
            field = value
            if (value) {
                setPadding(context.getDimensionPixelSize(R.dimen.offsetMedium))
            } else {
                setPaddingHorizontal(context.getDimensionPixelSize(R.dimen.offsetMedium))
            }
        }

    init {
        setPaddingHorizontal(context.getDimensionPixelSize(R.dimen.offsetMedium))
        orientation = HORIZONTAL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = if (multiLine) context.getDimensionPixelSize(R.dimen.bigItemHeight) else context.getDimensionPixelSize(R.dimen.itemHeight)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
    }
}