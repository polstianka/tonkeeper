package uikit.widget

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsets
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.marginBottom
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import uikit.R
import uikit.extensions.activity
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.range
import uikit.extensions.roundTop
import uikit.extensions.scale
import uikit.extensions.setPaddingBottom
import uikit.extensions.setView
import kotlin.math.abs

class ModalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    companion object {
        private const val parentScale = .92f
        private const val parentAlpha = .8f
    }

    var doOnHide: (() -> Unit)? = null

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                doOnHide?.invoke()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (slideOffset > 0f) {
                return
            }
            val progress = 1f - abs(slideOffset)
            if (progress >= 0f) {
                onAnimationUpdateParent(progress)
            }
        }
    }

    private val bgView: View
    private val coordinatorView: CoordinatorLayout
    private val bottomSheetView: FrameLayout
    private val isAnimating: Boolean
        get() {
            return try {
                behavior.state == BottomSheetBehavior.STATE_DRAGGING || behavior.state == BottomSheetBehavior.STATE_SETTLING
            } catch (e: Exception) {
                true
            }
        }

    val behavior: BottomSheetBehavior<FrameLayout>
    var fragment: Fragment? = null
    var scaleBackground: Boolean = false

    private val parentRootView: View? by lazy {
        val v = context.activity?.findViewById<View>(R.id.root_container)
        v
    }

    init {
        inflate(context, R.layout.view_modal, this)
        bgView = findViewById(R.id.bg)

        bottomSheetView = findViewById(R.id.modal_design_bottom_sheet)
        bottomSheetView.roundTop(context.getDimensionPixelSize(R.dimen.cornerMedium))
        bottomSheetView.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            setPeekHeight(v.measuredHeight)
        }
        bottomSheetView.setOnClickListener { }

        coordinatorView = findViewById(R.id.modal_coordinator)

        behavior = BottomSheetBehavior.from(bottomSheetView)
        behavior.addBottomSheetCallback(bottomSheetCallback)
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
        behavior.isFitToContents = true

        findViewById<View>(R.id.modal_touch_outside).setOnClickListener {
            hide(false)
        }
        doOnLayout { onAnimationUpdateParent(0f) }
    }

    override fun requestLayout() {
        if (isAnimating) {
            return
        }
        super.requestLayout()
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        val compatInsets = WindowInsetsCompat.toWindowInsetsCompat(insets)
        val statusInsets = compatInsets.getInsets(WindowInsetsCompat.Type.statusBars())
        applyTopInsets(statusInsets.top + context.getDimensionPixelSize(R.dimen.offsetMedium))

        val navigationInsets = compatInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
        val keyboardInsets = compatInsets.getInsets(WindowInsetsCompat.Type.ime())
        applyBottomInsets(keyboardInsets.bottom + navigationInsets.bottom)

        return super.onApplyWindowInsets(insets)
    }

    private fun applyTopInsets(newTopMargin: Int) {
        val layoutParams = bottomSheetView.layoutParams as CoordinatorLayout.LayoutParams
        val oldTopMargin = layoutParams.topMargin
        if (newTopMargin != oldTopMargin) {
            layoutParams.updateMargins(top = newTopMargin)
            requestLayout()
        }
    }

    private fun applyBottomInsets(newBottomPadding: Int) {
        val oldBottomPadding = bottomSheetView.marginBottom
        if (newBottomPadding != oldBottomPadding) {
            bottomSheetView.setPaddingBottom(newBottomPadding)
        }
    }

    fun setContentView(view: View) {
        bottomSheetView.setView(view)
    }

    fun hide(force: Boolean) {
        Log.d("ModalViewLog", "hide!")
        if (force) {
            behavior.isHideable = true
        }
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun onDetachedFromWindow() {
        onAnimationUpdateParent(0f)
        super.onDetachedFromWindow()
    }

    private fun onAnimationUpdateParent(progress: Float) {
        bgView.alpha = progress

        if (!scaleBackground) {
            return
        }
        val parentView = parentRootView ?: return
        val radius = context.getDimensionPixelSize(R.dimen.cornerMedium)
        parentView.roundTop(progress.range(0, radius))
        parentView.scale = progress.range(1f, parentScale)
        parentView.alpha = progress.range(1f, parentAlpha)
    }

    private fun setPeekHeight(newPeekHeight: Int) {
        post {
            behavior.peekHeight = newPeekHeight
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun hasOverlappingRendering() = false
}