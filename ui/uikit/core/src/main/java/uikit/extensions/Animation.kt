package uikit.extensions

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.Animation
import androidx.core.animation.doOnEnd

inline fun Animation.doOnEnd(
    crossinline action: (animation: Animation) -> Unit
): Animation.AnimationListener =
    setListener(onEnd = action)

inline fun Animation.doOnStart(
    crossinline action: (animation: Animation) -> Unit
): Animation.AnimationListener =
    setListener(onStart = action)

inline fun Animation.doOnRepeat(
    crossinline action: (animation: Animation) -> Unit
): Animation.AnimationListener =
    setListener(onRepeat = action)

inline fun Animation.setListener(
    crossinline onEnd: (animation: Animation) -> Unit = {},
    crossinline onStart: (animation: Animation) -> Unit = {},
    crossinline onRepeat: (animation: Animation) -> Unit = {}
): Animation.AnimationListener {
    val listener = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {
            onStart(animation)
        }

        override fun onAnimationEnd(animation: Animation) {
            onEnd(animation)
        }

        override fun onAnimationRepeat(animation: Animation) {
            onRepeat(animation)
        }
    }

    setAnimationListener(listener)
    return listener
}

fun toggleVisibilityAnimation(
    fromView: View,
    toView: View,
    duration: Long = 180L,
) {
    if (toView.visibility == View.VISIBLE && fromView.visibility == View.GONE) {
        return
    }

    toView.visibility = View.VISIBLE
    toView.alpha = 0f

    val fadeOutAnimator = ObjectAnimator.ofFloat(fromView, View.ALPHA, 1f, 0f)
    val fadeInAnimator = ObjectAnimator.ofFloat(toView, View.ALPHA, 0f, 1f)

    val animationSet = AnimatorSet()
    animationSet.duration = duration
    animationSet.playTogether(fadeOutAnimator, fadeInAnimator)
    animationSet.doOnEnd {
        fromView.visibility = View.GONE
    }
    animationSet.start()
}

fun makeVisibleAnimation(
    views: List<View>,
    duration: Long = 180L,
) {

    val animationSet = AnimatorSet()
    animationSet.duration = duration

    animationSet.playTogether(views.filter { it.visibility != View.VISIBLE || it.alpha != 1.0f }.map {
        it.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(it, View.ALPHA, it.alpha, 1f)
    })
    animationSet.start()
}