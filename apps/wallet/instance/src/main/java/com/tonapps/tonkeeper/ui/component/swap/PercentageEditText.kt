package com.tonapps.tonkeeper.ui.component.swap

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.tonapps.tonkeeperx.R
import uikit.drawable.InputDrawable

class PercentageEditText
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : EditText(context, attrs),
        View.OnFocusChangeListener {
        private var isProgrammaticallyChangingText = false
        private val percentageColor =
            ContextCompat.getColor(context, com.tonapps.uikit.color.R.color.textSecondaryDark)
        private val inputDrawable = InputDrawable(context)

        init {
            setup()
        }

        private fun setup() {
            background = inputDrawable
            inputType = EditorInfo.TYPE_CLASS_NUMBER
            onFocusChangeListener = this
            addPercentageSymbol()

            addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int,
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int,
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        if (!isProgrammaticallyChangingText) {
                            isProgrammaticallyChangingText = true
                            removePercentageSymbol()
                            addPercentageSymbol()
                            validateInput()
                            applySpan()
                            setCorrectSelection()
                            isProgrammaticallyChangingText = false
                        }
                    }
                },
            )
        }

        private fun addPercentageSymbol() {
            if (!text.toString().contains(" %")) {
                append(" %")
            }
        }

        private fun removePercentageSymbol() {
            val currentText = text.toString()
            if (currentText.contains(" %")) {
                setText(currentText.replace(" %", ""))
            }
        }

        override fun onFocusChange(
            v: View?,
            hasFocus: Boolean,
        ) {
            inputDrawable.active = hasFocus
        }

        private fun validateInput() {
            val currentText = text.toString().replace(" %", "")
            if (currentText.isNotEmpty()) {
                val value = currentText.toInt()
                if (value < 0) {
                    setText("0 %")
                } else if (value > 100) {
                    setText("100 %")
                } else {
                    setText("$value %")
                }
            }
        }

        private fun setCorrectSelection() {
            val textLength = text?.length ?: 0
            if (textLength > 2) {
                setSelection(textLength - 2)
            }
        }

        private fun applySpan() {
            val currentText = text.toString()
            val spannable = SpannableStringBuilder(currentText)

            val length = currentText.length
            if (length > 2) {
                spannable.setSpan(
                    ForegroundColorSpan(percentageColor),
                    length - 2,
                    length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
            text = spannable
        }

        override fun onSelectionChanged(
            selStart: Int,
            selEnd: Int,
        ) {
            if (!isProgrammaticallyChangingText) {
                val textLength = text?.length ?: 0
                if (selStart >= textLength - 1 || selEnd >= textLength - 1) {
                    setCorrectSelection()
                } else {
                    super.onSelectionChanged(selStart, selEnd)
                }
            }
        }
    }
