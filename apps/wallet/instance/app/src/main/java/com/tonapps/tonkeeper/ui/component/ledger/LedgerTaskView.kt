package com.tonapps.tonkeeper.ui.component.ledger

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeperx.R
import uikit.widget.RowLayout

class LedgerTaskView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RowLayout(context, attrs, defStyle) {

    private val dotIconView: View
    private val doneIconView: View
    private val loaderView: View
    private val labelView: AppCompatTextView

    var label: CharSequence
        get() = labelView.text
        set(value) {
            labelView.text = value
        }

    init {
        inflate(context, R.layout.view_ledger_task, this)

        dotIconView = findViewById(R.id.dot_icon)
        doneIconView = findViewById(R.id.done_icon)
        loaderView = findViewById(R.id.loader)
        labelView = findViewById(R.id.label)
    }

    fun setDefault() {
        dotIconView.visibility = View.VISIBLE
        doneIconView.visibility = View.GONE
        loaderView.visibility = View.GONE
    }

    fun setDone() {
        dotIconView.visibility = View.GONE
        doneIconView.visibility = View.VISIBLE
        loaderView.visibility = View.GONE
    }

    fun setLoading() {
        dotIconView.visibility = View.GONE
        doneIconView.visibility = View.GONE
        loaderView.visibility = View.VISIBLE
    }
}