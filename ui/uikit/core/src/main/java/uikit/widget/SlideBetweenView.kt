package uikit.widget

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.animation.PathInterpolatorCompat

class SlideBetweenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    private var currentIndex: Int = 0

    fun next() {
        val fromView = getChildAt(currentIndex)
        val toView = getChildAt(currentIndex + 1)

        toView.visibility = VISIBLE
        toView.translationX = measuredWidth.toFloat()

        playAnimators(
            ObjectAnimator.ofFloat(fromView, "translationX", -measuredWidth.toFloat()),
            ObjectAnimator.ofFloat(toView, "translationX", 0f)
        ) {
            currentIndex++
            fromView.visibility = GONE
        }
    }

    fun prev() {
        val fromView = getChildAt(currentIndex) ?: return
        val toView = getChildAt(currentIndex - 1) ?: return

        toView.visibility = VISIBLE
        toView.translationX = -measuredWidth.toFloat()

        playAnimators(
            ObjectAnimator.ofFloat(fromView, "translationX", measuredWidth.toFloat()),
            ObjectAnimator.ofFloat(toView, "translationX", 0f)
        ) {
            currentIndex--
            fromView.visibility = GONE
        }
    }

    fun getCurrentIndex(): Int {
        return currentIndex
    }

    private companion object {
        private const val ANIMATION_DURATION = 220L
        private val INTERPOLATOR = PathInterpolatorCompat.create(0.4f, 0f, 0.2f, 1f)

        private fun playAnimators(
            vararg animators: Animator,
            doOnEnd: ((animator: Animator) -> Unit)? = null
        ) {
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(animators.toList())
            animatorSet.duration = ANIMATION_DURATION
            animatorSet.interpolator = INTERPOLATOR
            doOnEnd?.let(animatorSet::doOnEnd)
            animatorSet.start()
        }
    }
}