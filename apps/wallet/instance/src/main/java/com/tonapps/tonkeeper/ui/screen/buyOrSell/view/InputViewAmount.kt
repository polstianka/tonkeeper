package com.tonapps.tonkeeper.ui.screen.buyOrSell.view

import android.animation.ValueAnimator
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
import uikit.widget.LoaderView

class InputViewAmount @JvmOverloads constructor(
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
                }
            }
        }

    private var visibleClearView: Boolean = false
        set(value) {
            if (disableClearButton) {
                return
            }
            field = value
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
                }
            }
        }

    private val hintAnimation = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 80
        addUpdateListener(this@InputViewAmount)
    }

    private val inputDrawable = InputDrawable(context)
    private val hintView: AppCompatTextView
    val editText: AppCompatEditText
    private val optionsView: View
    private val actionView: AppCompatTextView
    private val txTypeValue: AppCompatTextView




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
                } else {
                    if (hintReduced) {
                        visibleClearView = true
                    }
                }
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

        }

    var text: String
        get() = editText.text.toString()
        set(value) {
            val text = editText.text ?: return
            text.replace(0, text.length, value)
        }


    fun setTextToTxTypeValue(newText:String) {
        txTypeValue.text = newText
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

        inflate(context, com.tonapps.tonkeeperx.R.layout.input_view_amount, this)

        hintView = findViewById(R.id.input_hint)
        editText = findViewById(R.id.input_field)
        editText.onFocusChangeListener = this
        editText.addTextChangedListener(this)
        editText.setCursorColor(context.accentBlueColor)

        optionsView = findViewById(R.id.input_options)
        actionView = findViewById(R.id.input_action)
        txTypeValue = findViewById(com.tonapps.tonkeeperx.R.id.input_type_currency)


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