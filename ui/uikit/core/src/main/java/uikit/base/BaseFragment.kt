package uikit.base

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.BackEventCompat
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tonapps.uikit.color.backgroundPageColor
import uikit.extensions.getSpannable
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.BottomSheetLayout
import uikit.widget.ModalView
import uikit.widget.SwipeBackLayout
import java.util.concurrent.Executor

open class BaseFragment(
    @LayoutRes layoutId: Int
): Fragment(layoutId) {

    interface PredictiveBackGesture {
        fun onPredictiveBackCancelled() {

        }

        fun onPredictiveBackProgressed(backEvent: BackEventCompat) {

        }

        fun onPredictiveOnBackStarted(backEvent: BackEventCompat) {

        }
    }

    interface SwipeBack: PredictiveBackGesture {

        fun onEndShowingAnimation() {

        }
    }

    interface BottomSheet {

        fun onEndShowingAnimation() {

        }

        fun onDragging() {

        }
    }

    interface Modal {

        private val fragment: BaseFragment
            get() = this as BaseFragment

        private val view: ModalView
            get() = fragment.view as ModalView

        val behavior: BottomSheetBehavior<FrameLayout>
            get() = view.behavior

        val scaleBackground: Boolean
            get() = false

        fun onEndShowingAnimation() {

        }
    }

    interface CustomBackground {

    }

    val window: Window?
        get() = activity?.window

    val parent: Fragment?
        get() {
            if (parentFragment != null) {
                return parentFragment
            }
            return activity?.supportFragmentManager?.fragments?.lastOrNull {
                it != this && it.isVisible
            }
        }

    val mainExecutor: Executor
        get() = ContextCompat.getMainExecutor(requireContext())

    open val disableShowAnimation: Boolean = false

    open val secure: Boolean = false

    private var isFinished: Boolean = false

    fun setArgs(args: BaseArgs) {
        arguments = args.toBundle()
    }

    fun getSpannable(@StringRes id: Int): SpannableString {
        return requireContext().getSpannable(id)
    }

    fun registerForPermission(callback: ActivityResultCallback<Boolean>): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.RequestPermission(), callback)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = super.onCreateView(inflater, container, savedInstanceState)!!

        val view = if (this is Modal) {
            wrapInModal(inflater.context, contentView, savedInstanceState)
        } else {
            if (this !is CustomBackground) {
                contentView.setBackgroundColor(requireContext().backgroundPageColor)
            }
            when (this) {
                is SwipeBack -> wrapInSwipeBack(inflater.context, contentView, savedInstanceState)
                is BottomSheet -> wrapInBottomSheet(inflater.context, contentView, savedInstanceState)
                else -> contentView
            }
        }
        view.setOnClickListener {  }
        return view
    }

    private fun wrapInModal(context: Context, view: View, savedInstanceState: Bundle?): ModalView {
        this as Modal

        val modalView = ModalView(context)
        modalView.scaleBackground = scaleBackground
        modalView.setContentView(view)
        modalView.doOnHide = { finishInternal() }
        modalView.fragment = this
        if (savedInstanceState == null && !disableShowAnimation) {
            onEndShowingAnimation()
        } else {
            modalView.doOnLayout { onEndShowingAnimation() }
        }
        return modalView
    }

    private fun wrapInSwipeBack(context: Context, view: View, savedInstanceState: Bundle?): SwipeBackLayout {
        this as SwipeBack

        val swipeBackLayout = SwipeBackLayout(context)
        swipeBackLayout.doOnCloseScreen = ::finishInternal
        swipeBackLayout.doOnEndShowingAnimation = ::onEndShowingAnimation
        swipeBackLayout.setContentView(view)
        if (savedInstanceState == null && !disableShowAnimation) {
            swipeBackLayout.startShowAnimation()
        } else {
            swipeBackLayout.doOnLayout { onEndShowingAnimation() }
        }
        return swipeBackLayout
    }

    private fun wrapInBottomSheet(context: Context, view: View, savedInstanceState: Bundle?): BottomSheetLayout {
        this as BottomSheet

        val bottomSheetLayout = BottomSheetLayout(context)
        bottomSheetLayout.setContentView(view)
        bottomSheetLayout.doOnHide = ::finishInternal
        bottomSheetLayout.doOnAnimationEnd = ::onEndShowingAnimation
        bottomSheetLayout.doOnDragging = ::onDragging
        bottomSheetLayout.fragment = this
        if (savedInstanceState == null && !disableShowAnimation) {
            bottomSheetLayout.startShowAnimation()
        } else {
            bottomSheetLayout.doOnLayout { onEndShowingAnimation() }
        }
        return bottomSheetLayout
    }

    open fun onBackPressed(): Boolean {
        finish()
        return false
    }

    open fun finish() {
        if (isFinished) {
            return
        }

        val view = view ?: return

        isFinished = true

        when (view) {
            is SwipeBackLayout -> view.startHideAnimation()
            is BottomSheetLayout -> view.hide(true)
            is ModalView -> view.hide(true)
            else -> finishInternal()
        }
    }

    private fun finishInternal() {
        navigation?.remove(this)
    }

    override fun onResume() {
        super.onResume()
        if (secure) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        (view as? BottomSheetLayout)?.show()
    }

    override fun onPause() {
        super.onPause()
        if (secure) {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    fun removeCallbacks(callback: Runnable) {
        view?.removeCallbacks(callback)
    }

    fun postDelayed(delay: Long, action: Runnable) {
        view?.postDelayed(action, delay)
    }

    fun post(action: Runnable) {
        view?.post(action)
    }

    @ColorInt
    fun getColor(@ColorRes colorRes: Int): Int {
        return requireContext().getColor(colorRes)
    }

    fun getDrawable(
        drawableRes: Int,
        @ColorInt tintColor: Int = Color.TRANSPARENT
    ): Drawable {
        val drawable = ContextCompat.getDrawable(requireContext(), drawableRes)!!
        if (tintColor != Color.TRANSPARENT) {
            drawable.setTint(tintColor)
        }
        return drawable
    }

    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    fun getCurrentFocus(): EditText? {
        return requireActivity().currentFocus as? EditText
    }

}