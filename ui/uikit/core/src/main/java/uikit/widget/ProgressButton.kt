package uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatButton
import uikit.R

class ProgressButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    private val progressButton: AppCompatButton
    private val progressBar: ProgressBar
    private var text = ""

    var onClick: ((view: View, isEnabled : Boolean) -> Unit)? = null
        set(value) {
            field = value
            progressButton.setOnClickListener {
                value?.invoke(it, isEnabled)
            }
        }

    init {

        inflate(context, R.layout.view_progress_button, this)

        progressButton = findViewById(R.id.progress_button)
        progressBar = findViewById(R.id.progress_bar)

    }

    fun setText(text: String) {
        this.text = text
        progressButton.text = text
    }

    fun toggleProgressDisplay(display: Boolean) {
        if (display) {
            progressButton.text = ""
            progressBar.visibility = View.VISIBLE
            progressButton.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            progressButton.text = text
            progressButton.isEnabled = true
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (enabled) {
            progressButton.setBackgroundResource(R.drawable.bg_button_primary)
        } else {
            progressButton.setBackgroundResource(R.drawable.bg_button_secondary)
        }
    }


}