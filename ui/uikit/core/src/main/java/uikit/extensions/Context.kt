package uikit.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.annotation.AnimRes
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.annotation.StyleableRes
import androidx.core.content.res.ResourcesCompat
import com.tonapps.uikit.color.backgroundHighlightedColor
import com.tonapps.uikit.color.stateList
import com.tonapps.uikit.color.textTertiaryColor
import uikit.R

fun Context.inflate(
    @LayoutRes layoutId: Int,
    root: ViewGroup? = null,
    attachToRoot: Boolean = false
): View {
    return LayoutInflater.from(this).inflate(layoutId, root, attachToRoot)
}

fun Context.getDimensionPixelSize(@DimenRes id: Int): Int {
    return resources.getDimensionPixelSize(id)
}

fun Context.getDimension(@DimenRes id: Int): Float {
    return resources.getDimension(id)
}

fun Context.getAnimation(@AnimRes id: Int) = AnimationUtils.loadAnimation(this, id)

fun Context.getSpannable(@StringRes id: Int): SpannableString {
    return getText(id).processAnnotation(this)
}

@SuppressLint("DiscouragedApi")
@ColorInt
fun Context.getColorByIdentifier(name: String): Int {
    return resources.getIdentifier(name, "color", packageName).let {
        getColor(it)
    }
}

val Context.activity: ComponentActivity?
    get() {
        var context = this
        while (context is ContextWrapper) {
            if (context is ComponentActivity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

val Context.window: Window?
    get() = activity?.window

fun Context.useAttributes(
    set: AttributeSet?,
    @StyleableRes attrs: IntArray,
    block: (TypedArray) -> Unit) {
    theme.obtainStyledAttributes(set, attrs, 0, 0).apply {
        try {
            block(this)
        } finally {
            recycle()
        }
    }
}

fun Context.createRipple(
    content: Drawable? = null,
    mask: Drawable? = null
): RippleDrawable {
    return RippleDrawable(backgroundHighlightedColor.stateList, content, mask)
}

fun Context.textWithLabel(text: String, label: CharSequence?): CharSequence {
    if (label.isNullOrEmpty()) {
        return text
    }
    val span = SpannableString("$text $label")
    span.setSpan(ForegroundColorSpan(textTertiaryColor), text.length, text.length + label.length + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return span
}

fun Context.drawable(id: Int): Drawable {
    return ResourcesCompat.getDrawable(resources, id, theme)!!
}

fun Context.getCurrentFocus(): View? {
    return activity?.currentFocus
}

fun Context.getCurrentFocusEditText(): EditText? {
    return getCurrentFocus() as? EditText
}

val Context.cornerMedium: Int
    get() = resources.getDimensionPixelSize(R.dimen.cornerMedium)