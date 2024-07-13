package uikit.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import uikit.R
import uikit.extensions.useAttributes

class TextHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ColumnLayout(context, attrs, defStyle) {

    private val titleView: AppCompatTextView
    private val descriptionView: AppCompatTextView

    var title: String
        get() = titleView.text.toString()
        set(value) {
            titleView.text = value
        }

    var desciption: String
        get() = descriptionView.text.toString()
        set(value) {
            descriptionView.text = value
        }

    init {
        inflate(context, R.layout.view_text_header, this)

        titleView = findViewById(R.id.title)
        descriptionView = findViewById(R.id.description)

        context.useAttributes(attrs, R.styleable.TextHeaderView) {
            titleView.text = it.getString(R.styleable.TextHeaderView_android_title)
            descriptionView.text = it.getString(R.styleable.TextHeaderView_android_description)
        }
    }
}