package uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.uikit.color.stateList
import uikit.R
import uikit.extensions.createRipple
import uikit.extensions.useAttributes

class IconButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    private val iconView: AppCompatImageView
    private val titleView: AppCompatTextView
    private val ripple = context.createRipple()

    init {
        inflate(context, R.layout.view_icon_button, this)
        background = ripple

        iconView = findViewById(R.id.icon)
        titleView = findViewById(R.id.title)

        context.useAttributes(attrs, R.styleable.IconButtonView) {
            iconView.setImageResource(it.getResourceId(R.styleable.IconButtonView_android_icon, 0))
            if(it.getString(R.styleable.IconButtonView_android_title) == ""){
                titleView.visibility = INVISIBLE
            }
            else{
                titleView.text = it.getString(R.styleable.IconButtonView_android_title)
            }
            isEnabled = it.getBoolean(R.styleable.IconButtonView_android_enabled, true)

            val tintColor = it.getColor(R.styleable.IconButtonView_android_tint, 0)
            if (tintColor != 0) {
                iconView.imageTintList = tintColor.stateList
                titleView.setTextColor(tintColor)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        ripple.radius = measuredHeight / 2
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alpha = if (enabled) 1f else .2f
    }
}