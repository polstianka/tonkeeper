package uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import uikit.R
import uikit.extensions.dimen
import uikit.extensions.useAttributes

class PreferenceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ConstraintLayout(context, attrs, defStyle) {

    private val titleView: TextView
    private val descriptionView: TextView
    private val switchView: SwitchView

    var doOnCheckedChanged: ((Boolean) -> Unit)?
        get() = switchView.doCheckedChanged
        set(value) {
            switchView.doCheckedChanged = value
        }

    var title: String
        get() = titleView.text.toString()
        set(value) {
            titleView.text = value
        }

    var description: String
        get() = descriptionView.text.toString()
        set(value) {
            descriptionView.text = value
        }

    var checked: Boolean
        get() = switchView.checked
        set(value) {
            switchView.checked = value
        }

    init {
        inflate(context, R.layout.view_preference, this)
        setPadding(context.dimen(R.dimen.offsetMedium))
        setBackgroundResource(R.drawable.bg_button_content_16)

        titleView = findViewById(R.id.title)
        descriptionView = findViewById(R.id.description)
        switchView = findViewById(R.id.check)

        setOnClickListener {
            checked = !checked
        }

        context.useAttributes(attrs, R.styleable.PreferenceView) {
            title = it.getString(R.styleable.PreferenceView_android_title) ?: title
            description = it.getString(R.styleable.PreferenceView_android_description)
                ?: description
            checked = it.getBoolean(R.styleable.PreferenceView_android_checked, false)
        }
    }

}