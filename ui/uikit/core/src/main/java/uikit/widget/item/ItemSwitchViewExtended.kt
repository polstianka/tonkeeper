package uikit.widget.item

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.setPadding
import com.tonapps.uikit.list.ListCell
import uikit.R
import uikit.extensions.drawable
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.useAttributes
import uikit.widget.SwitchView

class ItemSwitchViewExtended @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyle) {

    private val textView: AppCompatTextView
    private val subtitleView: AppCompatTextView
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

    var subtitle: String?
        get() = subtitleView.text.toString()
        set(value) {
            subtitleView.text = value
        }

    var checked: Boolean
        get() = switchView.checked
        set(value) {
            switchView.checked = value
        }

    init {
        inflate(context, R.layout.view_item_switch_extended, this)
        background = ListCell.Position.SINGLE.drawable(context)
        setPadding(context.getDimensionPixelSize(R.dimen.offsetMedium))
        textView = findViewById(R.id.text)
        switchView = findViewById(R.id.check)
        subtitleView = findViewById(R.id.subtitle)

        setOnClickListener {
            checked = !checked
        }

        context.useAttributes(attrs, R.styleable.ItemSwitchView) {
            text = it.getString(R.styleable.ItemSwitchView_android_text)
            subtitle = it.getString(R.styleable.ItemSwitchView_android_subtitle)
            checked = it.getBoolean(R.styleable.ItemSwitchView_android_checked, false)
        }
    }

}