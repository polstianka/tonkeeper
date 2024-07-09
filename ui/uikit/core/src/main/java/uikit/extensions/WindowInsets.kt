package uikit.extensions

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

val insetsBottomTypeMask: Int
    get() = WindowInsetsCompat.Type.ime() or WindowInsetsCompat.Type.systemBars()

val WindowInsetsCompat.bottomBars: Insets
    get() = getInsets(insetsBottomTypeMask)

val WindowInsetsCompat.bottomBarsOffset: Int
    get() = bottomBars.bottom

fun View.applyNavBottomPadding(addition: Float) = applyNavBottomPadding(addition.toInt())

fun View.applyNavBottomPadding(addition: Int = 0) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        val navInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        updatePadding(bottom = navInsets.bottom + addition)
        insets
    }
}