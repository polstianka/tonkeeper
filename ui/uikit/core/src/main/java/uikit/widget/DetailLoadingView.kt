package uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.setPaddingHorizontal
import com.tonapps.uikit.list.ListCell
import uikit.R
import uikit.extensions.dimen
import uikit.extensions.drawable
import uikit.extensions.useAttributes

class DetailLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    var position: ListCell.Position? = null
        set(value) {
            if (field != value) {
                field = value
                background = value?.drawable(context)
            }
        }

    private val titleView: AppCompatTextView
    private val loaderView: LoaderView

    var title: CharSequence?
        get() = titleView.text
        set(value) {
            titleView.text = value
        }

    var isLoading: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                loaderView.isVisible = value
                if (value) {
                    loaderView.startAnimation()
                } else {
                    loaderView.stopAnimation()
                }
            }
        }

    init {
        inflate(context, R.layout.view_detail_loading, this)
        setPaddingHorizontal(context.getDimensionPixelSize(uikit.R.dimen.offsetMedium))

        titleView = findViewById(R.id.title)
        loaderView = findViewById(R.id.loader)

        context.useAttributes(attrs, R.styleable.DetailDescriptionView) {
            title = it.getString(R.styleable.DetailDescriptionView_android_title) ?: title
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = context.dimen(R.dimen.detailViewLoadingHeight)
        val heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightSpec)
    }

}