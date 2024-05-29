package uikit.widget

import android.animation.ValueAnimator
import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
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
import uikit.extensions.isLtr
import uikit.extensions.isRtl
import uikit.extensions.pivot
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

    companion object {
        private const val DEFAULT_ANIMATION_DURATION = 80L
        private const val STICKY_SUFFIX_ANIMATION_DURATION = 110L
    }

    private val hintAnimation = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = DEFAULT_ANIMATION_DURATION
        addUpdateListener(this@InputView)
    }

    private val inputDrawable = InputDrawable(context)
    private val hintView: AppCompatTextView
    private val suffixView: AppCompatTextView
    private val editText: AppCompatEditText
    private val optionsView: View
    private val actionView: AppCompatTextView
    private val iconView: AppCompatImageView
    private val clearView: AppCompatImageView
    private val loaderView: LoaderView

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

    var active: Boolean
        get() = inputDrawable.active
        set(value) {
            inputDrawable.active = value
        }

    fun forceActive(value: Boolean) = inputDrawable.forceActive(value)

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

    var suffix: CharSequence
        get() = suffixView.text
        set(value) {
            suffixView.text = value
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
                editText.filters = if (value > 0) arrayOf(InputFilter.LengthFilter(value)) else emptyArray()
            }
        }

    var inputType: Int
        get() = editText.inputType
        set(value) {
            editText.inputType = value
        }

    var imeOptions: Int
        get() = editText.imeOptions
        set(value) {
            editText.imeOptions = value
        }

    init {
        background = inputDrawable
        minimumHeight = context.getDimensionPixelSize(R.dimen.barHeight)

        inflate(context, R.layout.view_input, this)

        hintView = findViewById(R.id.input_hint)
        suffixView = findViewById(R.id.input_suffix)

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
            val inputType = it.getInt(R.styleable.InputView_android_inputType, EditorInfo.TYPE_NULL)
            if (inputType != EditorInfo.TYPE_NULL) {
                this.inputType = inputType
            }
            val imeOptions = it.getInt(R.styleable.InputView_android_imeOptions, 0)
            if (imeOptions != 0) {
                this.imeOptions = imeOptions
            }
            val digits = it.getString(R.styleable.InputView_android_digits)
            if (!digits.isNullOrEmpty()) {
                editText.keyListener = DigitsKeyListener.getInstance(digits.toString());
            }
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

    fun clearInput() {
        text = ""
        error = false
        if (!activateDrawableOnFocus) {
            active = false
        }
    }

    var activateDrawableOnFocus = true

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (activateDrawableOnFocus) {
            inputDrawable.active = hasFocus
        }
    }

    enum class ReductionAnimationType {
        DEFAULT,
        STICKY_SUFFIX
    }

    private val directionFactor: Float
        get() = if (isRtl()) -1.0f else 1.0f

    private var lastNonEmptySuffixOffset: Float = 0.0f

    private fun updateSuffixOffset(): Boolean {
        val progress = hintAnimation.animatedValue as Float
        val lineWidth = maxOf(0.0f, minOf(
            (editText.measuredWidth - editText.paddingLeft - editText.paddingRight).toFloat(),
            editText.layout?.takeIf { it.lineCount > 0 }?.getLineWidth(0) ?: 0.0f
        ))
        if (lineWidth > 0.0f) {
            lastNonEmptySuffixOffset = lineWidth
        }
        suffixView.translationX = progress.range(
            hintView.measuredWidth.toFloat(),
            lastNonEmptySuffixOffset
        ) * directionFactor
        return true
    }

    var reductionAnimationType = ReductionAnimationType.DEFAULT
        set(value) {
            if (field == value) return
            field = value
            when (value) {
                ReductionAnimationType.DEFAULT -> {
                    suffixView.visibility = GONE
                    editText.viewTreeObserver.removeOnDrawListener(::updateSuffixOffset)
                    hintAnimation.duration = DEFAULT_ANIMATION_DURATION
                }
                ReductionAnimationType.STICKY_SUFFIX -> {
                    suffixView.visibility = VISIBLE
                    editText.viewTreeObserver.addOnDrawListener(::updateSuffixOffset)
                    hintView.setLayerType(LAYER_TYPE_HARDWARE, null)
                    suffixView.setLayerType(LAYER_TYPE_HARDWARE, null)
                    hintAnimation.duration = STICKY_SUFFIX_ANIMATION_DURATION
                }
            }
        }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val progress = animation.animatedValue as Float

        when (reductionAnimationType) {
            ReductionAnimationType.DEFAULT -> {
                hintView.pivot = 0.0f
                hintView.scale = progress.range(expandHintConfig.hintScale, reduceHintConfig.hintScale)
                hintView.translationY = progress.range(expandHintConfig.hintTranslationY, reduceHintConfig.hintTranslationY)
                editText.translationY = progress.range(expandHintConfig.editTextTranslationY, reduceHintConfig.editTextTranslationY)
            }
            ReductionAnimationType.STICKY_SUFFIX -> {
                updateSuffixOffset()
                hintView.alpha = 1.0f - progress
                hintView.scale = progress.range(1.0f, 0.35f)
                hintView.pivotY = hintView.paddingTop + (hintView.measuredHeight - hintView.paddingTop - hintView.paddingBottom).toFloat() / 2.0f
                hintView.pivotX = if (isLtr()) {
                    hintView.measuredWidth.toFloat()
                } else {
                    0.0f
                }
                hintView.translationX = suffixView.translationX - (hintView.measuredWidth.toFloat() - suffixView.measuredWidth * progress) * directionFactor
            }
        }
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