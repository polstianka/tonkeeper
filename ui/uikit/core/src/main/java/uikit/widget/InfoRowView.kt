package uikit.widget

import android.content.Context
import android.content.res.ColorStateList
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.TextViewCompat
import com.tonapps.uikit.color.UIKitColor
import com.tonapps.uikit.color.accentGreenColor
import com.tonapps.uikit.color.accentOrangeColor
import com.tonapps.uikit.color.accentRedColor
import com.tonapps.uikit.color.resolveColor
import uikit.R
import uikit.extensions.updateVisibility
import uikit.extensions.useAttributes

class InfoRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ConstraintLayout(context, attrs, defStyle) {

    private val titleView: AppCompatTextView
    private val valueView: AppCompatTextView
    var hint: String = ""
        private set

    enum class Style {
        NORMAL, ERROR, WARNING, POSITIVE
    }

    var titleStyle: Style = Style.NORMAL
        set(newStyle) {
            if (field == newStyle) return
            field = newStyle
            titleView.applyStyle(newStyle, UIKitColor.textSecondaryColor)
        }

    var valueStyle: Style = Style.NORMAL
        set(newStyle) {
            if (field == newStyle) return
            field = newStyle
            valueView.applyStyle(newStyle, UIKitColor.textPrimaryColor)
        }

    var title: CharSequence
        get() = titleView.text
        set(value) {
            titleView.text = value
        }

    var value: CharSequence
        get() = valueView.text
        set(value) {
            valueView.text = value
            val visibility = if (value.isEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
            if (valueView.updateVisibility(visibility)) {
                titleView.ellipsize = if (visibility == View.GONE) {
                    TextUtils.TruncateAt.END
                } else {
                    TextUtils.TruncateAt.MIDDLE
                }
            }
        }

    @DrawableRes
    var titleIcon: Int = 0
        set(newIconResource) {
            if (field == newIconResource) return
            field = newIconResource
            titleView.applyIcon(newIconResource)
        }

    init {
        inflate(context, R.layout.view_info_row, this)

        titleView = findViewById(R.id.info_title)
        valueView = findViewById(R.id.info_value)

        context.useAttributes(attrs, R.styleable.InfoRowView) {
            titleView.text = it.getString(R.styleable.InfoRowView_android_title)
            titleIcon = it.getResourceId(R.styleable.InfoRowView_android_icon, 0)
            value = it.getString(R.styleable.InfoRowView_android_value) ?: ""
            hint = it.getString(R.styleable.InfoRowView_android_hint) ?: ""
        }
    }

    companion object {
        private fun AppCompatTextView.applyStyle(style: Style, @AttrRes normalColorRef: Int) {
            val color = when (style) {
                Style.NORMAL -> {
                    context.resolveColor(normalColorRef)
                }
                Style.ERROR -> {
                    context.accentRedColor
                }
                Style.WARNING -> {
                    context.accentOrangeColor
                }
                Style.POSITIVE -> {
                    context.accentGreenColor
                }
            }
            setTextColor(color)
            TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(color))
        }

        private fun AppCompatTextView.applyIcon(@DrawableRes drawableRes: Int) {
            val drawable = if (drawableRes != 0) {
                AppCompatResources.getDrawable(context, drawableRes)
            } else {
                null
            }
            setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null)
        }
    }
}