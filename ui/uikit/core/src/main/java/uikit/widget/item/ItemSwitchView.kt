package uikit.widget.item

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import uikit.R
import uikit.extensions.useAttributes
import uikit.widget.SwitchView

class ItemSwitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : BaseItemView(context, attrs, defStyle) {

    private val textView: AppCompatTextView by lazy {
        findViewById(R.id.text)
    }
    private val hintView: AppCompatTextView? by lazy {
        findViewById(R.id.description)
    }
    private val switchView: SwitchView by lazy {
        findViewById(R.id.check)
    }

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

    var hint: String?
        get() = hintView?.text.toString()
        set(value) {
            hintView?.text = value
        }

    var checked: Boolean
        get() = switchView.checked
        set(value) {
            switchView.checked = value
        }

    init {
        context.useAttributes(attrs, R.styleable.ItemSwitchView) {
            val resolvedHint = it.getString(R.styleable.ItemSwitchView_android_hint)
            if (resolvedHint.isNullOrEmpty()) {
                inflate(context, R.layout.view_item_switch, this)
            } else {
                // TODO: use single XML file for both modes.
                // Separate layout is only needed because of the unknown purpose of constant height
                // in BaseItemView.onMeasure, so in order to not break anything, separate one is used.
                inflate(context, R.layout.view_item_switch_desc, this)
                enableWeirdConstantContentDimension = false
            }

            text = it.getString(R.styleable.ItemSwitchView_android_text)
            hint = resolvedHint
            checked = it.getBoolean(R.styleable.ItemSwitchView_android_checked, false)
            position = com.tonapps.uikit.list.ListCell.from(it.getString(R.styleable.ItemSwitchView_position))
        }

        setOnClickListener {
            this.checked = !checked
        }
    }

}