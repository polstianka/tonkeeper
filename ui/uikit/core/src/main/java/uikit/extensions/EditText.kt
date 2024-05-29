package uikit.extensions

import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import java.lang.reflect.Field


fun EditText.cursorToEnd() {
    try {
        setSelection(text.length)
    } catch (ignored: Throwable) { }
}

fun EditText.requestFocusWithSelection() {
    requestFocus()
    cursorToEnd()
}

fun EditText.requestFocusWithSelectionAndKeyboard() {
    requestFocus()
    cursorToEnd()
    getInsetsControllerCompat()?.show(WindowInsetsCompat.Type.ime())
}

fun EditText.focusWithKeyboard() {
    doOnLayout {
        post {
            requestFocusWithSelection()
            getInsetsControllerCompat()?.show(WindowInsetsCompat.Type.ime())
        }
    }
}

fun EditText.hideKeyboard() {
    clearFocus()
    getInsetsControllerCompat()?.hide(WindowInsetsCompat.Type.ime())
}

fun EditText.setCursorColor(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        textCursorDrawable?.setTint(color)
    } else {
        setCursorColorLegacy(color)
    }
}

private fun EditText.setCursorColorLegacy(color: Int) {
    try {
        val f: Field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
        f.isAccessible = true
        val mCursorDrawableRes: Int = f.getInt(this)
        val f1: Field = TextView::class.java.getDeclaredField("mEditor")
        f1.isAccessible = true
        val editor: Any = f1.get(this)
        val clazz: Class<*> = editor.javaClass
        val f2: Field = clazz.getDeclaredField("mCursorDrawable")
        f2.isAccessible = true
        val drawables: Array<Drawable?> = arrayOfNulls(2)
        drawables[0] = context.getDrawable(mCursorDrawableRes)
        drawables[1] = context.getDrawable(mCursorDrawableRes)
        drawables[0]?.setTint(color)
        drawables[1]?.setTint(color)
        f2.set(editor, drawables)
    } catch (ignored: Throwable) { }
}