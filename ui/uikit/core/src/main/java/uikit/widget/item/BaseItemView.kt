package uikit.widget.item

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.LinearLayoutCompat
import com.tonapps.uikit.list.ListCell
import uikit.R
import uikit.extensions.drawable
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.setPaddingHorizontal
import uikit.extensions.useAttributes

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
    private var isHeightHardcoded: Boolean = true

    init {
        setPaddingHorizontal(context.getDimensionPixelSize(R.dimen.offsetMedium))
        orientation = HORIZONTAL
        context.useAttributes(attrs, R.styleable.BaseItemView) { typedArray ->
            isHeightHardcoded = typedArray.getBoolean(
                R.styleable.BaseItemView_isHeightHardcoded,
                true
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMeasureSpec = if (isHeightHardcoded) {
            val height = context.getDimensionPixelSize(R.dimen.itemHeight)
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        } else {
            heightMeasureSpec
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}