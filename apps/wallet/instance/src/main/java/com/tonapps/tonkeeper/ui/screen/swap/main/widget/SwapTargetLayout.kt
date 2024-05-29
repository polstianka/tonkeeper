package com.tonapps.tonkeeper.ui.screen.swap.main.widget

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.tonapps.tonkeeper.core.widget.AmountInputView
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapAmount
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapConfig
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapTarget
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.api.entity.TokenEntity
import uikit.animator.BoolAnimator
import uikit.effect.FadeAndScaleVisibilityEffect
import uikit.effect.FadeAndScaleVisibilityEffect.Companion.applyFadeAndScaleVisibilityAffect
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.setMarginBottom
import uikit.extensions.setPaddingBottom
import uikit.extensions.useAttributes
import uikit.widget.StretchyViewGroup
import java.math.BigDecimal

class SwapTargetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ConstraintLayout(context, attrs, defStyle) {
    companion object {
        private const val GONE_SCALE = .45f
    }

    private val titleView: AppCompatTextView

    private val balanceView: AppCompatTextView
    private val balanceActionButton: AppCompatTextView

    private val tokenDuet: StretchyViewGroup
    private val otherTokenView: SwapTokenView
    private val currentTokenView: SwapTokenView

    val amountInput: AmountInputView

    private var target: SwapTarget = SwapTarget.SEND
        private set(value) {
            field = value
            if (SwapConfig.APPLY_WEIRD_RECEIVE_WRAP_BOTTOM_PADDING) {
                val paddingBottom: Int
                val amountMarginBottomDp: Float
                when (target) {
                    SwapTarget.SEND -> {
                        paddingBottom = context.getDimensionPixelSize(uikit.R.dimen.offsetMedium)
                        amountMarginBottomDp = 20.0f
                    }
                    SwapTarget.RECEIVE -> {
                        paddingBottom = context.getDimensionPixelSize(uikit.R.dimen.offsetExtraSmall)
                        amountMarginBottomDp = 8.0f
                    }
                }
                if (tokenDuet.paddingBottom != paddingBottom) {
                    tokenDuet.setPaddingBottom(paddingBottom)
                    val bottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, amountMarginBottomDp, context.resources.displayMetrics).toInt()
                    amountInput.setMarginBottom(bottomMargin)
                }
            }
        }
    var balanceActionButtonEnabled: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            balanceActionButton.isEnabled = value
        }
    var inputEnabled: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            amountInput.setEnabled(value)
        }
    var balanceText: String = ""
        set(value) {
            if (field == value) return
            field = value
            if (value.isNotEmpty()) {
                balanceView.text = value
            }
            balanceVisible.changeValue(value.isNotEmpty(), animationsEnabled())
        }
    var isAmountInsufficient: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            amountInput.isInsufficient = value
        }
    var token: TokenEntity? = null
        set(value) {
            field = value
            amountInput.decimals = token?.decimals ?: AmountInputView.DECIMALS_UNSPECIFIED
            // TODO animate change between two tokens?
            tokenVisible.setIsVisible(value != null, animationsEnabled)
            if (value != null) {
                currentTokenView.token = value
            }
        }

    init {
        inflate(context, R.layout.view_swap_target, this)

        titleView = findViewById(android.R.id.title)
        balanceView = findViewById(R.id.balance)
        balanceActionButton = findViewById(R.id.balance_action)

        tokenDuet = findViewById(R.id.token_duet)
        otherTokenView = tokenDuet.findViewById(android.R.id.empty)
        currentTokenView = tokenDuet.findViewById(R.id.token)

        amountInput = findViewById(R.id.amount)
        amountInput.enableAutoFit = true

        context.useAttributes(attrs, R.styleable.SwapTargetView) {
            titleView.text = it.getString(R.styleable.SwapTargetView_android_title)

            val targetAttr = it.getString(R.styleable.SwapTargetView_target)
            target = when (targetAttr) {
                "1", "receive" -> SwapTarget.RECEIVE
                else -> SwapTarget.SEND
            }
        }
    }

    var animationsEnabled: () -> Boolean = { true }
        set(value) {
            field = value
            amountInput.animationsEnabled = value
        }

    val visibleTokenView: SwapTokenView
        get() = if (tokenVisible.value) {
            currentTokenView
        } else {
            otherTokenView
        }

    private val offsetExtraSmall by lazy {
        context.getDimensionPixelSize(uikit.R.dimen.offsetExtraSmall)
    }
    private val offsetMedium by lazy {
        context.getDimensionPixelSize(uikit.R.dimen.offsetMedium)
    }

    private fun updateBalanceOffset() {
        balanceView.translationX = if (balanceVisible.state == BoolAnimator.State.TRUE && balanceActionButton.visibility != View.GONE) {
            (balanceActionButton.measuredWidth - (offsetMedium - offsetExtraSmall)) * (1.0f - balanceActionButtonVisible.floatValue)
        } else 0.0f
    }

    val balanceActionButtonVisible = BoolAnimator(FadeAndScaleVisibilityEffect.DURATION, DecelerateInterpolator()) { state, animatedValue, stateChanged, prevState ->
        balanceActionButton.applyFadeAndScaleVisibilityAffect(animatedValue)
        if (balanceVisible.state == BoolAnimator.State.TRUE) {
            updateBalanceOffset()
        }
    }
    private val balanceVisible = BoolAnimator(FadeAndScaleVisibilityEffect.DURATION, DecelerateInterpolator()) {  state, animatedValue, stateChanged, prevState ->
        balanceView.applyFadeAndScaleVisibilityAffect(animatedValue)
        if (stateChanged && (prevState == BoolAnimator.State.TRUE || state == BoolAnimator.State.TRUE)) {
            updateBalanceOffset()
        }
    }
    private val tokenVisible = FadeAndScaleVisibilityEffect(
        currentTokenView,
        initialValue = false,
        goneScale = GONE_SCALE
    ) { visibility ->
        val reverseVisibility = 1f - visibility
        otherTokenView.applyFadeAndScaleVisibilityAffect(reverseVisibility)
        val direction = if (target == SwapTarget.SEND) {
            -1.0f
        } else {
            1.0f
        }
        otherTokenView.translationY = otherTokenView.measuredHeight.toFloat() * .85f * direction * visibility
        currentTokenView.translationY = -currentTokenView.measuredHeight.toFloat() * .85f * direction * reverseVisibility
    }

    fun setOnTokenClickListener(onClickListener: (View) -> Unit) {
        otherTokenView.setOnClickListener(onClickListener)
        currentTokenView.setOnClickListener(onClickListener)
    }

    fun setOnTokenLongClickListener(onLongClickListener: (View) -> Boolean) {
        otherTokenView.setOnLongClickListener(onLongClickListener)
        currentTokenView.setOnLongClickListener(onLongClickListener)
    }

    fun doOnBalanceActionClick(listener: OnClickListener) =
        balanceActionButton.setOnClickListener(listener)

    fun doOnAmountChange(onAmountChange: (view: AmountInputView, amount: BigDecimal, formatted: String) -> Unit) {
        amountInput.doOnDecimalValueChange(onAmountChange)
    }

    fun forceAmount(amount: SwapAmount?) {
        if (amount == null || amount.amount.stringRepresentation == amountInput.hint) {
            amountInput.setText("")
        } else {
            amountInput.setDecimalValueOptimized(amount.amount.number, amount.amount.stringRepresentation)
        }
    }
}