package uikit.widget

import android.animation.ValueAnimator
import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.uikit.color.accentBlueColor
import com.tonapps.uikit.color.accentRedColor
import uikit.R
import uikit.drawable.InputDrawable
import uikit.extensions.dp
import uikit.extensions.focusWithKeyboard
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.hideKeyboard
import uikit.extensions.range
import uikit.extensions.scale
import uikit.extensions.setCursorColor
import uikit.extensions.useAttributes


class InputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle),
    View.OnFocusChangeListener,
    TextWatcher,
    ValueAnimator.AnimatorUpdateListener {

    private val reduceHintConfig = HintConfig(
        hintScale = .75f,
        hintTranslationY = (-4f).dp,
        editTextTranslationY = 12f.dp,
    )

    private val expandHintConfig = HintConfig(
        hintScale = 1f,
        hintTranslationY = 0f,
        editTextTranslationY = 6f.dp,
    )

    var disableClearButton: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (value) {
                    clearView.visibility = View.GONE
                }
            }
        }

    private var visibleClearView: Boolean = false
        set(value) {
            if (disableClearButton) {
                return
            }
            field = value
            clearView.visibility = if (value && isEnabled) View.VISIBLE else View.GONE
        }

    private var hintReduced = false
        set(value) {
            if (field != value) {
                field = value
                if (value) {
                    hintAnimation.start()
                    optionsView.visibility = View.GONE
                    visibleClearView = true
                } else {
                    hintAnimation.reverse()
                    optionsView.visibility = View.VISIBLE
                    visibleClearView = false
                    loaderView.visibility = View.GONE
                    loaderView.stopAnimation()
                }
            }
        }

    private val hintAnimation = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 80
        addUpdateListener(this@InputView)
    }

    private val inputDrawable = InputDrawable(context)
    private val hintView: AppCompatTextView
    private val editText: AppCompatEditText
    private val optionsView: View
    private val actionView: AppCompatTextView
    private val iconView: AppCompatImageView
    private val clearView: AppCompatImageView
    private val loaderView: LoaderView
    private var initializedDecimalFilter = false

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

    var loading: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (value && hintReduced) {
                    visibleClearView = false
                    loaderView.visibility = View.VISIBLE
                    loaderView.startAnimation()
                } else {
                    if (hintReduced) {
                        visibleClearView = true
                    }
                    loaderView.visibility = View.GONE
                    loaderView.stopAnimation()
                }
            }
        }

    fun setDecimalFilter(maxValue: Int) {
        if(!initializedDecimalFilter){
            editText.filters = editText.filters.plus(DecimalDigitsInputFilter(maxValue))
            initializedDecimalFilter = true
        }
        else{
            editText.filters =
                editText.filters.filter{ it.javaClass != DecimalDigitsInputFilter::class.java}.toTypedArray()
            editText.filters = editText.filters.plus(DecimalDigitsInputFilter(maxValue))
        }
    }

    private var actionValue: String? = null
        set(value) {
            field = value
            actionView.text = value
            actionView.visibility = if (value.isNullOrBlank()) View.GONE else View.VISIBLE
        }

    private var iconValue: Int = 0
        set(value) {
            field = value
            iconView.setImageResource(value)
            iconView.visibility = if (value == 0) View.GONE else View.VISIBLE
        }

    var doOnTextChange: ((String) -> Unit)? = null
    var doOnButtonClick: (() -> Unit)? = null
        set(value) {
            field = value
            actionView.setOnClickListener {
                value?.invoke()
            }
        }

    var doOnIconClick: (() -> Unit)? = null
        set(value) {
            field = value
            iconView.setOnClickListener {
                value?.invoke()
            }
        }

    var text: String
        get() = editText.text.toString()
        set(value) {
            val text = editText.text ?: return
            text.replace(0, text.length, value)
        }


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

    init {
        background = inputDrawable
        minimumHeight = context.getDimensionPixelSize(R.dimen.barHeight)

        inflate(context, R.layout.view_input, this)

        hintView = findViewById(R.id.input_hint)
        editText = findViewById(R.id.input_field)
        editText.onFocusChangeListener = this
        editText.addTextChangedListener(this)
        editText.setCursorColor(context.accentBlueColor)

        optionsView = findViewById(R.id.input_options)
        actionView = findViewById(R.id.input_action)
        iconView = findViewById(R.id.input_icon)
        clearView = findViewById(R.id.input_clear)
        loaderView = findViewById(R.id.input_loader)

        clearView.setOnClickListener {
            if (isEnabled) {
                editText.text = null
            }
        }

        context.useAttributes(attrs, R.styleable.InputView) {
            hintView.text = it.getString(R.styleable.InputView_android_hint)
            iconValue = it.getResourceId(R.styleable.InputView_android_icon, 0)
            actionValue = it.getString(R.styleable.InputView_android_button)
            isEnabled = it.getBoolean(R.styleable.InputView_android_enabled, true)
            singleLine = it.getBoolean(R.styleable.InputView_android_singleLine, false)
            maxLength = it.getInt(R.styleable.InputView_android_maxLength, 0)
            disableClearButton = it.getBoolean(R.styleable.InputView_disableClearButton, false)
        }

        setOnClickListener {
            editText.focusWithKeyboard()
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        editText.isEnabled = enabled
        visibleClearView = visibleClearView
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

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        inputDrawable.active = hasFocus
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val progress = animation.animatedValue as Float
        hintView.scale = progress.range(expandHintConfig.hintScale, reduceHintConfig.hintScale)
        hintView.translationY =
            progress.range(expandHintConfig.hintTranslationY, reduceHintConfig.hintTranslationY)
        editText.translationY = progress.range(
            expandHintConfig.editTextTranslationY,
            reduceHintConfig.editTextTranslationY
        )
    }

    private data class HintConfig(
        val hintScale: Float,
        val hintTranslationY: Float,
        val editTextTranslationY: Float,
    )

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable?) {
        hintReduced = !s.isNullOrBlank()
        doOnTextChange?.invoke(s.toString())
    }

}

class DecimalDigitsInputFilter(private var maxValue: Int) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        try {
            val input: String =
                dest!!.subSequence(0, dstart).toString() + source.toString() + dest.subSequence(
                    dend,
                    dest.length
                )
            if (input.isEmpty()) {
                return null
            }
            val value = input.toFloat()
            if (input.contains(".") && input.substring(input.indexOf(".") + 1).length > 1) {
                return ""
            }
            if (!input.contains(".") && input.toInt().toString().length < input.length) {
                return ""
            }
            if (value > maxValue) {
                return ""
            }
        } catch (e: NumberFormatException) {
            return ""
        }
        return null
    }
}