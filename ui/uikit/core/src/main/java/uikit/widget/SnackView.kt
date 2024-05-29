package uikit.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.setPadding
import com.tonapps.uikit.color.backgroundContentTintColor
import uikit.R
import uikit.extensions.dp
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.hapticConfirm

class SnackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RowLayout(context, attrs, defStyle) {

    private val handler = Handler(Looper.getMainLooper())
    private val textView: AppCompatTextView

    private val close: ImageView

    init {
        inflate(context, R.layout.view_snack, this)
        setBackgroundResource(R.drawable.bg_content_tint_24)
        setPadding(context.getDimensionPixelSize(R.dimen.offsetMedium))
        visibility = View.GONE
        textView = findViewById(R.id.snack_text)
        close = findViewById(R.id.image)
        close.setOnClickListener {
            hideNow()
        }
    }

    fun setText(text: CharSequence) {
        textView.text = text
    }

    fun show(text: CharSequence, color: Int = context.backgroundContentTintColor) {
        setText(text)
        visibility = VISIBLE
        background.setTint(color)
        hideDelayed()
    }

    fun hideNow() {
        handler.removeCallbacks(hide)
        hide.run()
    }

    private fun hideDelayed() {
        handler.removeCallbacks(hide)
        handler.postDelayed(hide, 5000)
    }

    val hide = Runnable { visibility = View.GONE }

}