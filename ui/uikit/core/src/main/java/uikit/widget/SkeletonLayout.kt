package uikit.widget

import android.content.Context
import android.util.AttributeSet
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout

open class SkeletonLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ShimmerFrameLayout(context, attrs, defStyle) {

    init {
        shimmer = Shimmer.AlphaHighlightBuilder()
            .setDuration(1000)
            .setBaseAlpha(0.7f)
            .setHighlightAlpha(1f)
            .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
            .setAutoStart(true)
            .build()

        startShimmer()
    }
}