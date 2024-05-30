package uikit.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatButton
import uikit.R
import uikit.extensions.useAttributes

class ProgressButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    private val progressButton: AppCompatButton
    private val progressBar: ProgressBar
    private var text = ""

    var onClick: ((view: View, isEnabled: Boolean) -> Unit)? = null
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

        context.useAttributes(attrs, R.styleable.ProgressButton) {
            progressButton.text = it.getString(R.styleable.ProgressButton_android_text)
        }

    }

    fun setText(text: String) {
        this.text = text
        // progressButton.text = text

        val fadeOut = ObjectAnimator.ofFloat(progressButton, "alpha", 1f, 0f)
        fadeOut.duration = 200
        fadeOut.interpolator = DecelerateInterpolator()

        fadeOut.addUpdateListener {
            if (it.animatedFraction == 1f) {
                progressButton.text = text
                val fadeIn = ObjectAnimator.ofFloat(progressButton, "alpha", 0f, 1f)
                fadeIn.duration = 200
                fadeIn.interpolator = DecelerateInterpolator()
                fadeIn.start()
            }
        }
        fadeOut.start()
    }

    fun toggleProgressDisplay(display: Boolean) {
        if (display) {
            progressButton.text = ""
            progressBar.visibility = View.VISIBLE
            progressButton.isEnabled = false

            val fadeIn = ObjectAnimator.ofFloat(progressBar, "alpha", 0f, 1f)
            fadeIn.duration = 500
            fadeIn.interpolator = DecelerateInterpolator()
            fadeIn.start()
        } else {
            val fadeOut = ObjectAnimator.ofFloat(progressBar, "alpha", 1f, 0f)
            fadeOut.duration = 500
            fadeOut.interpolator = DecelerateInterpolator()
            fadeOut.addUpdateListener {
                if (it.animatedFraction == 1f) {
                    progressBar.visibility = View.GONE
                    progressButton.text = text
                    progressButton.isEnabled = true
                }
            }
            fadeOut.start()

//            progressBar.visibility = View.GONE
//            progressButton.text = text
//            progressButton.isEnabled = true
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        progressButton.isEnabled = enabled

        if (enabled) {
            setBackgroundResource(R.drawable.bg_button_primary)
            // progressButton.setTextColor(context.buttonPrimaryForegroundColor)
        } else {
            setBackgroundResource(R.drawable.bg_button_secondary)
            // progressButton.setTextColor(context.buttonSecondaryForegroundColor)
        }
    }

    fun setEnabled(enableState: EnableState) {
        when (enableState) {
            EnableState.EnableActiveColor -> {
                progressButton.isEnabled = true
                setBackgroundResource(R.drawable.bg_button_primary)
            }

            EnableState.EnableDeactiveColor -> {
                progressButton.isEnabled = true
                setBackgroundResource(R.drawable.bg_button_secondary)
            }

            EnableState.Disable -> {
                progressButton.isEnabled = false
                setBackgroundResource(R.drawable.bg_button_secondary)
            }

            EnableState.DisableIgnoreBackground -> {
                progressButton.isEnabled = false
            }
        }
    }

    enum class EnableState {
        EnableActiveColor, EnableDeactiveColor, Disable, DisableIgnoreBackground
    }


}