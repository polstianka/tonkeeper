package uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import uikit.R
import uikit.drawable.HeaderDrawable
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.setPaddingHorizontal
import uikit.extensions.useAttributes

class ModalHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RowLayout(context, attrs, defStyle) {

    private val barHeight = context.getDimensionPixelSize(R.dimen.barHeight)

    private val drawable = HeaderDrawable(context)
    private val titleView: AppCompatTextView
    private val closeView: View

    var onCloseClick: (() -> Unit)? = null
        set(value) {
            field = value
            closeView.setOnClickListener { value?.invoke() }
        }
    var text: String = ""
        set(value) {
            field = value
            titleView.text = value
        }

    init {
        super.setBackground(drawable)
        inflate(context, R.layout.view_modal_header, this)
        setPaddingHorizontal(context.getDimensionPixelSize(R.dimen.offsetMedium))
        titleView = findViewById(R.id.modal_header_title)
        closeView = findViewById(R.id.modal_header_close)

        context.useAttributes(attrs, R.styleable.ModalHeader) {
            text = it.getString(R.styleable.ModalHeader_android_text).toString()
        }
    }

    fun setDivider(value: Boolean) {
        drawable.setDivider(value)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(barHeight, MeasureSpec.EXACTLY))
    }
}

