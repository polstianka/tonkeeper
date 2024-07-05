package uikit.widget

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import com.tonapps.uikit.color.accentBlueColor
import com.tonapps.uikit.color.accentRedColor
import uikit.R
import uikit.drawable.InputDrawable
import uikit.extensions.focusWithKeyboard
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.hideKeyboard
import uikit.extensions.setCursorColor
import uikit.extensions.useAttributes

class PercentInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle),
    View.OnFocusChangeListener,
    TextWatcher {

    private val inputDrawable = InputDrawable(context)

    private val container: View
    private val editText: AppCompatEditText

    var activeBorderColor: Int
        get() = inputDrawable.activeBorderColor
        set(value) {
            inputDrawable.activeBorderColor = value
        }

    var error: Boolean
        get() = inputDrawable.error
        set(value) {
            inputDrawable.error = value
            if (value) {
                editText.setCursorColor(context.accentRedColor)
            } else {
                editText.setCursorColor(context.accentBlueColor)
            }
        }


    var doOnTextChange: ((String) -> Unit)? = null

    var text: String
        get() = editText.text.toString()
        set(value) {
            val text = editText.text ?: return
            text.replace(0, text.length, value)
        }

    var hint: String = ""

    val isEmpty: Boolean
        get() = text.isBlank()

    var singleLine: Boolean = false
        set(value) {
            editText.isSingleLine = value
            field = value
        }

    var maxLength: Int = 0
        set(value) {
            if (field != value) {
                field = value
                editText.filters =
                    if (value > 0) arrayOf(InputFilter.LengthFilter(value)) else emptyArray()
            }
        }

    var inputType: Int
        get() = editText.inputType
        set(value) {
            editText.inputType = value
        }

    var color: Int
        get() = editText.currentTextColor
        set(value) {
            editText.setTextColor(value)
        }

    init {
        background = inputDrawable
        minimumHeight = context.getDimensionPixelSize(R.dimen.barHeight)

        inflate(context, R.layout.view_percent_input, this)

        container = findViewById(R.id.percent_input_container)
        editText = findViewById(R.id.input_field)
        editText.onFocusChangeListener = this
        editText.addTextChangedListener(this)
        editText.setCursorColor(context.accentBlueColor)

        context.useAttributes(attrs, R.styleable.InputView) {
            isEnabled = it.getBoolean(R.styleable.InputView_android_enabled, true)
            singleLine = it.getBoolean(R.styleable.InputView_android_singleLine, false)
            maxLength = it.getInt(R.styleable.InputView_android_maxLength, 0)
        }

        setOnClickListener {
            editText.focusWithKeyboard()
        }


    }

    private fun isValidInput(input: String): Boolean {
        val regex = Regex("^\\d{0,2}(\\.\\d{0,1})?\$")
        return input.matches(regex)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        editText.isEnabled = enabled
    }

    fun onEditorAction(actionCode: Int) {
        editText.onEditorAction(actionCode)
    }

    fun setOnEditorActionListener(listener: TextView.OnEditorActionListener) {
        editText.setOnEditorActionListener(listener)
    }

    fun setOnDoneActionListener(listener: () -> Unit) {
        onEditorAction(EditorInfo.IME_ACTION_DONE)
        setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                listener()
                true
            } else {
                false
            }
        }
    }

    fun focus() {
        postDelayed({
            editText.focusWithKeyboard()
        }, 16)
    }

    fun hideKeyboard() {
        editText.hideKeyboard()
    }

    fun clear() {
        editText.text?.clear()
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        inputDrawable.active = hasFocus
    }


    private var previousText = ""

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        previousText = s.toString()
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        error = !isValidInput(s.toString())
    }

    override fun afterTextChanged(s: Editable?) {
        val input = s.toString()
        if (isValidInput(input)) {
            previousText = input
            error = false
        } else {
            editText.setText(previousText)
            editText.setSelection(previousText.length)
            error = true
        }

        doOnTextChange?.invoke(input)

        if (input.isEmpty()) {
            editText.hint = hint
        } else {
            editText.hint = ""
        }
    }

    fun getInputAsFloat(): Float {
        return try {
            editText.text.toString().toFloat()
        } catch (e: NumberFormatException) {
            0f
        }
    }

}