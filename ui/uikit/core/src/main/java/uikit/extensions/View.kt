package uikit.extensions

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.Window
import android.view.animation.Animation
import android.widget.TextView
import androidx.annotation.AnimRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.animation.doOnEnd
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateMargins
import androidx.core.widget.NestedScrollView
import androidx.core.widget.NestedScrollView.OnScrollChangeListener
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import uikit.insets.KeyboardAnimationCallback
import kotlin.math.sin

var View.scale: Float
    get() = scaleX
    set(value) {
        scaleX = value
        scaleY = value
    }

var View.pivot: Float
    get() = pivotX
    set(value) {
        pivotX = value
        pivotY = value
    }

val View.isVisibleForUser: Boolean
    get() = isAttachedToWindow && isShown && windowVisibility == View.VISIBLE

val View.window: Window?
    get() = context.window

fun View.getInsetsControllerCompat(): WindowInsetsControllerCompat? {
    val window = window ?: return null
    return WindowInsetsControllerCompat(window, this)
}

fun View.getRootWindowInsetsCompat(): WindowInsetsCompat? {
    return ViewCompat.getRootWindowInsets(this)
}

val View.statusBarHeight: Int
    get() = getRootWindowInsetsCompat()?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0

fun ViewGroup.inflate(
    @LayoutRes
    layoutRes: Int
): View {
    return context.inflate(layoutRes, this, false)
}

fun View.setPaddingTop(paddingTop: Int) {
    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
}

fun View.setPaddingBottom(paddingBottom: Int) {
    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
}

fun View.setPaddingStart(paddingStart: Int) {
    setPadding(paddingStart, paddingTop, paddingRight, paddingBottom)
}

fun View.setPaddingEnd(paddingEnd: Int) {
    setPadding(paddingLeft, paddingTop, paddingEnd, paddingBottom)
}

fun View.setPaddingHorizontal(paddingHorizontal: Int) {
    setPadding(paddingHorizontal, paddingTop, paddingHorizontal, paddingBottom)
}

fun View.setPaddingVertical(paddingVertical: Int) {
    setPadding(paddingLeft, paddingVertical, paddingRight, paddingVertical)
}

fun ViewGroup.setView(view: View) {
    removeAllViews()
    addView(view)
}

fun View.roundTop(radius: Int) {
    if (radius == 0) {
        outlineProvider = null
        clipToOutline = false
    } else {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height + radius * 2, radius.toFloat())
            }
        }
        clipToOutline = true
    }
}

fun View.round(radius: Int) {
    if (radius == 0) {
        outlineProvider = null
        clipToOutline = false
    } else {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius.toFloat())
            }
        }
        clipToOutline = true
    }
}

fun View.getDrawable(@DrawableRes resId: Int): Drawable {
    return AppCompatResources.getDrawable(context, resId)!!
}

fun View.withAnimation(duration: Long = 120, block: () -> Unit) {
    if (this !is ViewGroup) {
        block()
        return
    }

    val transitionAdapter = object : TransitionListenerAdapter() {
        override fun onTransitionStart(transition: Transition) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }

        override fun onTransitionEnd(transition: Transition) {
            transition.removeListener(this)
            setLayerType(View.LAYER_TYPE_NONE, null)
        }
    }

    val transition = AutoTransition()
    transition.duration = duration
    transition.addListener(transitionAdapter)

    TransitionManager.beginDelayedTransition(this, transition)
    block()
}

fun TextView.setEndDrawable(drawable: Drawable?) {
    setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
}

fun TextView.setStartDrawable(drawable: Drawable?) {
    setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
}

fun TextView.setEndDrawable(@DrawableRes resId: Int) {
    setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(resId), null)
}

fun TextView.setStartDrawable(@DrawableRes resId: Int) {
    setCompoundDrawablesWithIntrinsicBounds(getDrawable(resId), null, null, null)
}

val ViewPager2.recyclerView: RecyclerView
    get() = getChildAt(0) as RecyclerView

val ViewPager2.layoutManager: RecyclerView.LayoutManager?
    get() = recyclerView.layoutManager

fun ViewPager2.findViewHolderForAdapterPosition(position: Int): RecyclerView.ViewHolder? {
    return recyclerView.findViewHolderForAdapterPosition(position)
}

fun View.hapticConfirm() {
    post {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }
}

fun View.hapticReject() {
    post {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }
}

fun View.runAnimation(@AnimRes resId: Int): Animation {
    val animation = context.getAnimation(resId)
    startAnimation(animation)
    return animation
}

fun View.startSnakeAnimation(
    count: Int = 3,
    offset: Int = 16.dp,
    duration: Long = 400
) {
    val animator = ValueAnimator.ofFloat(0f, 1f)
    animator.addUpdateListener { animation ->
        val x = animation.animatedValue as Float
        translationX = (4 * x * (1 - x) * sin(count * (x * Math.PI)) * offset).toFloat()
    }
    animator.doOnEnd {
        translationX = 0f
    }
    animator.duration = duration
    animator.start()
}

fun View.reject() {
    startSnakeAnimation()
    hapticReject()
}

inline fun View.doKeyboardAnimation(crossinline block: (
    offset: Int,
    progress: Float,
    isShowing: Boolean
) -> Unit) {
    val animationCallback = object : KeyboardAnimationCallback(this) {
        override fun onKeyboardOffsetChanged(offset: Int, progress: Float, isShowing: Boolean) {
            block(offset, progress, isShowing)
        }
    }
    ViewCompat.setWindowInsetsAnimationCallback(this, animationCallback)
}

fun View.pinToBottomInsets() {
    doKeyboardAnimation { offset, _, _ ->
        translationY = -offset.toFloat()
    }
}

fun View.applyBottomInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        view.translationY = -insets.bottomBarsOffset.toFloat()
        insets
    }
}

fun View.getViews(): List<View> {
    val result = mutableListOf<View>()
    if (this is ViewGroup) {
        for (i in 0 until childCount) {
            result.add(getChildAt(i))
        }
    }
    return result
}

fun TextView.setLeftDrawable(drawable: Drawable?) {
    setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
}

fun TextView.setRightDrawable(drawable: Drawable?) {
    setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
}

fun TextView.clearDrawables() {
    setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
}