package com.tonapps.tonkeeper.ui.screen.swap.view.progressButton

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.tonapps.tonkeeperx.R
import uikit.widget.LoaderView


class ProgressButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    private val button: Button
    private val loaderView: LoaderView

    fun updateTextInButton(newText: String) {
        button.text = newText
    }

    var buttonClickListener: (() -> Unit)? = null
        set(value)  {
            field = value
            button.setOnClickListener {
                value?.invoke()
            }
        }

    fun setUpLoading(state: Boolean) {
        if(state) {
            loaderView.isVisible = true
            updateTextInButton("")
        } else {
            loaderView.isVisible = false
        }
    }

    fun updateStateEnabledButton(stateEnabled: Boolean) {
        button.isEnabled = stateEnabled
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.progress_button_view, this, true)

        button = findViewById(R.id.button)
        loaderView = findViewById(R.id.loaderView)
    }
}