package uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

/**
 * There is a bug in navigation+{@link androidx.core.widget.NestedScrollView} gesture detection:
 * "fling down to close screen" doesn't work properly when touch_down started on touchable component
 * even though "swipe down and release at > half to close screen" works properly.
 *
 * This seems to be some bug in Navigation.kt that is triggered only by androidx.core.widget.NestedScrollView.
 *
 * TODO: Once fixed, replace all usages of this widget with NestedScrollView
 */
class AutoDisableNestedScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ScrollView(context, attrs, defStyle) {
    private var hasScrollableContent: Boolean = false
        private set(value) {
            if (field == value) return
            field = value
            checkTopScrolled()
        }

    var hasScrolled: Boolean = false
        private set(value) {
            if (field == value) return
            field = value
            checkTopScrolled()
        }

    var topScrolled: Boolean = false
        private set(value) {
            if (field == value) return
            field = value
            onTopScrolledStateChanged(value)
        }

    private fun checkTopScrolled() {
        this.topScrolled = hasScrolled && hasScrollableContent
    }

    var onTopScrolledStateChanged: (Boolean) -> Unit = { }

    init {
        setOnScrollChangeListener { _, _, scrollY, _, _ ->
            hasScrolled = scrollY != 0
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val viewportHeight = measuredHeight - paddingBottom - paddingTop
        var hasScrollableChild = false
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child != null && child.measuredHeight > viewportHeight) {
                hasScrollableChild = true
                break
            }
        }
        hasScrollableContent = hasScrollableChild
    }
}