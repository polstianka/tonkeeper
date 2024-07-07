package com.tonapps.tonkeeper.ui.screen.buysell

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.EditText

class PostfixTextWatcher(
    private val editText: EditText,
    private val postfix: String,
    private val onChange: (String) -> Unit,
) : TextWatcher {

    init {
        editText.accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun sendAccessibilityEvent(host: View, eventType: Int) {
                super.sendAccessibilityEvent(host, eventType)
                if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
                    val selection = editText.text.toString().length - postfix.length
                    editText.setSelection(selection.coerceAtLeast(0))
                }
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(s: Editable) {
        val inputText = s.toString()
        val current =
            if (inputText.contains(postfix)) inputText.dropLast(postfix.length) else inputText
        val combined = "$current$postfix"
        onChange(current)
        editText.removeTextChangedListener(this)
        editText.setText(combined)
        editText.setSelection(current.length)
        editText.addTextChangedListener(this)
    }
}