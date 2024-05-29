package uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import uikit.R
import uikit.extensions.dp
import uikit.extensions.setPaddingVertical
import uikit.extensions.useAttributes

class DescriptionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ColumnLayout(context, attrs, defStyle) {

    private val titleView: TextView
    private val descriptionView: TextView

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

    init {
        inflate(context, R.layout.view_description, this)
        setPaddingVertical(12.dp)

        titleView = findViewById(R.id.title)
        descriptionView = findViewById(R.id.description)

        context.useAttributes(attrs, R.styleable.DescriptionView) {
            title = it.getString(R.styleable.DescriptionView_android_title) ?: title
            description = it.getString(R.styleable.DescriptionView_android_description)
                ?: description
        }
    }
}