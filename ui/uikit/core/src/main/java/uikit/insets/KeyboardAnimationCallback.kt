package uikit.insets

import android.view.View
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import uikit.extensions.bottomBarsOffset
import uikit.extensions.getRootWindowInsetsCompat
import uikit.extensions.insetsBottomTypeMask

abstract class KeyboardAnimationCallback(
    private val view: View
): InsetsAnimationCallback(view, insetsBottomTypeMask) {

    protected val navigationOffset: Int by lazy {
        view.getRootWindowInsetsCompat()?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: 0
    }

    private val imeOffset: Int
        get() = view.getRootWindowInsetsCompat()?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0

    private val initOffset: Int by lazy {
        navigationOffset + imeOffset
    }

    private var lastOffset = 0
    private var isImeShown: Boolean = false

    init {
        view.doOnLayout {
            isImeShown = imeOffset > 0
            keyboardOffsetChanged(initOffset, 0f)
        }
    }

    override fun onUpdateInsets(insets: WindowInsetsCompat, animation: WindowInsetsAnimationCompat) {
        val fraction = animation.interpolatedFraction
        val offset = insets.bottomBarsOffset
        keyboardOffsetChanged(offset, fraction)
    }

    abstract fun onKeyboardOffsetChanged(offset: Int, progress: Float, isShowing: Boolean)

    private fun keyboardOffsetChanged(offset: Int, fraction: Float) {
        if (0 >= offset) {
            return
        }

        if (lastOffset == offset) {
            if (offset == initOffset) {
                val hide = 0 > (offset - lastOffset)
                onKeyboardOffsetChanged(offset, 0f, !hide)
            }
            return
        }

        val hide = 0 > (offset - lastOffset)
        val progress = if (hide) 1 - fraction else fraction
        onKeyboardOffsetChanged(offset, progress, !hide)
        lastOffset = offset
    }
}