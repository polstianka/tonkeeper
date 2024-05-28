package uikit.widget.item

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import uikit.R
import uikit.extensions.useAttributes
import uikit.widget.SwitchView

class ItemSwitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : BaseItemView(context, attrs, defStyle) {

    private val textView: AppCompatTextView
    private val descriptionView: AppCompatTextView
    private val switchView: SwitchView

    var doOnCheckedChanged: ((Boolean) -> Unit)?
        get() = switchView.doCheckedChanged
        set(value) {
            switchView.doCheckedChanged = value
        }

    var text: String?
        get() = textView.text.toString()
        set(value) {
            textView.text = value
        }

    var description: String?
        get() = descriptionView.text.toString()
        set(value) {
            descriptionView.text = value
            if (!value.isNullOrEmpty()) {
                descriptionView.visibility = View.VISIBLE
                multiLine = true
            } else {
                multiLine = false
            }
        }

    var checked: Boolean
        get() = switchView.checked
        set(value) {
            switchView.checked = value
        }

    init {
        inflate(context, R.layout.view_item_switch, this)

        textView = findViewById(R.id.text)
        descriptionView = findViewById(R.id.description)
        switchView = findViewById(R.id.check)

        setOnClickListener {
            checked = !checked
        }

        context.useAttributes(attrs, R.styleable.ItemSwitchView) {
            text = it.getString(R.styleable.ItemSwitchView_android_text)
            description = it.getString(R.styleable.ItemSwitchView_description)
            checked = it.getBoolean(R.styleable.ItemSwitchView_android_checked, false)
            position = com.tonapps.uikit.list.ListCell.from(it.getString(R.styleable.ItemSwitchView_position))
        }
    }

}