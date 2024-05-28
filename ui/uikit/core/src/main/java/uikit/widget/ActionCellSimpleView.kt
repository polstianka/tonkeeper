package uikit.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.setPadding
import com.tonapps.uikit.color.resolveColor
import com.tonapps.uikit.list.ListCell
import uikit.R
import uikit.extensions.dp
import uikit.extensions.drawable
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.useAttributes

class ActionCellSimpleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RowLayout(context, attrs, defStyle) {

    private val titleView: AppCompatTextView
    private val subtitleView: AppCompatTextView
    private val actionView: AppCompatImageView

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

    var actionRes: Int = 0
        set(value) {
            field = value
            if (value == 0) {
                actionView.visibility = GONE
            } else {
                actionView.visibility = VISIBLE
                actionView.setImageResource(value)
            }
        }

    var actionTint: Int = 0
        set(value) {
            field = value
            if (value == 0) {
                actionView.imageTintList = ColorStateList.valueOf(
                    context.resolveColor(com.tonapps.uikit.color.R.attr.accentBlueColor)
                )
            } else {
                actionView.imageTintList = ColorStateList.valueOf(context.resolveColor(value))
            }
        }

    var position: ListCell.Position = ListCell.Position.SINGLE
        set(value) {
            field = value
            background = value.drawable(context)
        }

    init {
        setPadding(context.getDimensionPixelSize(R.dimen.offsetMedium))
        minimumHeight = 76.dp
        position = ListCell.Position.SINGLE
        inflate(context, R.layout.view_action_cell_simple, this)

        titleView = findViewById(R.id.action_cell_title)
        subtitleView = findViewById(R.id.action_cell_subtitle)
        actionView = findViewById(R.id.action_cell_right)

        context.useAttributes(attrs, R.styleable.ActionCellView) {
            title = it.getString(R.styleable.ActionCellView_android_title)
            subtitle = it.getString(R.styleable.ActionCellView_android_subtitle)
            actionRes = it.getResourceId(R.styleable.HeaderView_android_action, 0)

            val singleLine = it.getBoolean(R.styleable.ActionCellView_android_singleLine, false)
            if (singleLine) {
                titleView.setSingleLine()
                subtitleView.setSingleLine()
            }
        }
    }
}