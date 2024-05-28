package uikit.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.uikit.color.resolveColor
import com.tonapps.uikit.list.ListCell
import uikit.R
import uikit.extensions.circle
import uikit.extensions.dp
import uikit.extensions.drawable
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.round
import uikit.extensions.useAttributes

class ActionCellRadioView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RowLayout(context, attrs, defStyle) {

    private val iconView: SimpleDraweeView
    private val titleView: AppCompatTextView
    private val subtitleView: AppCompatTextView
    private val titleBadge: AppCompatTextView
    private val radioButton: AppCompatRadioButton

    var title: CharSequence?
        get() = titleView.text
        set(value) {
            titleView.text = value
        }

    var subtitle: CharSequence?
        get() = subtitleView.text
        set(value) {
            subtitleView.text = value
        }

    var titleBadgeText: CharSequence?
        get() = titleBadge.text
        set(value) {
            titleBadge.isVisible = !value.isNullOrEmpty()
            titleBadge.text = value
        }

    var iconRes: Int = 0
        set(value) {
            field = value
            if (value == 0) {
                iconView.visibility = GONE
            } else {
                iconView.visibility = VISIBLE
                iconView.setImageResource(value)
            }
        }

    var iconVisible: Boolean
        set(value) {
            iconView.isVisible = value
        }
        get() = iconView.isVisible

    var iconTint: Int = 0
        set(value) {
            field = value
            if (value == 0) {
                iconView.imageTintList = null
            } else {
                iconView.imageTintList = ColorStateList.valueOf(context.resolveColor(value))
            }
        }

    var isRoundedIcon: Boolean = false
        set(value) {
            field = value
            if (value) iconView.circle() else iconView.round(0)
        }

    var position: ListCell.Position = ListCell.Position.SINGLE
        set(value) {
            field = value
            background = value.drawable(context)
        }

    var checked: Boolean
        get() = radioButton.isSelected
        set(value) {
            radioButton.isChecked = value
        }

    var onCheckedChange: () -> Unit = {}

    init {
        setPadding(context.getDimensionPixelSize(R.dimen.offsetMedium))
        minimumHeight = 76.dp
        position = ListCell.Position.SINGLE
        inflate(context, R.layout.view_action_cell_radio, this)

        iconView = findViewById(R.id.action_cell_icon)
        titleView = findViewById(R.id.action_cell_title)
        subtitleView = findViewById(R.id.action_cell_subtitle)
        titleBadge = findViewById(R.id.action_cell_title_badge)
        radioButton = findViewById(R.id.action_cell_radio_button)

        context.useAttributes(attrs, R.styleable.ActionCellView) {
            iconRes = it.getResourceId(R.styleable.HeaderView_android_icon, 0)
            title = it.getString(R.styleable.ActionCellView_android_title)
            subtitle = it.getString(R.styleable.ActionCellView_android_subtitle)

            val singleLine = it.getBoolean(R.styleable.ActionCellView_android_singleLine, false)
            if (singleLine) {
                titleView.setSingleLine()
                subtitleView.setSingleLine()
            }
        }
        radioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onCheckedChange()
            }
        }
    }
}