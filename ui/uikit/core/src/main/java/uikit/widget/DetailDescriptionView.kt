package uikit.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.setPaddingHorizontal
import com.tonapps.uikit.list.ListCell
import uikit.R
import uikit.extensions.dimen
import uikit.extensions.drawable
import uikit.extensions.useAttributes

class DetailDescriptionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyle) {

    var position: ListCell.Position? = null
        set(value) {
            if (field != value) {
                field = value
                background = value?.drawable(context)
            }
        }

    private val titleView: AppCompatTextView
    private val valueView: AppCompatTextView

    var title: CharSequence?
        get() = titleView.text
        set(value) {
            titleView.text = value
        }

    var value: CharSequence?
        get() = valueView.text
        set(value) {
            valueView.text = value
        }

    init {
        inflate(context, R.layout.view_detail_description, this)
        setPaddingHorizontal(context.getDimensionPixelSize(uikit.R.dimen.offsetMedium))
        orientation = HORIZONTAL

        titleView = findViewById(R.id.title)
        valueView = findViewById(R.id.value)

        context.useAttributes(attrs, R.styleable.DetailDescriptionView) {
            title = it.getString(R.styleable.DetailDescriptionView_android_title) ?: title
            value = it.getString(R.styleable.DetailDescriptionView_android_value) ?: value
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = context.dimen(R.dimen.detailViewDescriptionHeight)
        val heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightSpec)
    }

}