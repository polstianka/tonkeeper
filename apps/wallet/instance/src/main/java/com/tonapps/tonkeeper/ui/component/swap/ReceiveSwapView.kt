package com.tonapps.tonkeeper.ui.component.swap

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.doOnEnd
import androidx.core.view.isInvisible
import com.tonapps.tonkeeper.extensions.amount
import com.tonapps.tonkeeper.fragment.swap.model.Simulate
import com.tonapps.tonkeeper.fragment.swap.model.SwapState
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.textPrimaryColor
import com.tonapps.uikit.color.textTertiaryColor
import uikit.extensions.dimen
import uikit.extensions.dimenF
import uikit.extensions.dp
import uikit.widget.DetailDescriptionView
import uikit.widget.DetailLoadingView
import uikit.widget.DividerView


class ReceiveSwapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : BaseSwapView(context, attrs, defStyle), ValueAnimator.AnimatorUpdateListener {

    override fun layoutId() = R.layout.view_swap_receive

    private var changeAnimation: ValueAnimator? = null

    private val collapsedHeight: Int = context.dimen(R.dimen.swap_target_height)
    private val expandedHeight: Int =
        collapsedHeight + 1.dp + context.dimen(uikit.R.dimen.detailViewLoadingHeight) + 1.dp + (6 * context.dimen(
            uikit.R.dimen.detailViewDescriptionHeight
        )) + 8.dp

    private var isExpanded = false

    var onSnackShow: ((String) -> Unit)? = null

    private val dividerEndReceive: DividerView = findViewById(R.id.divider_end_receive)
    private val detailSwapRate: DetailLoadingView = findViewById(R.id.detail_swap_rate)
    private val dividerStartInfo: DividerView = findViewById(R.id.divider_start_info)
    private val detailPriceImpact: DetailDescriptionView = findViewById(R.id.detail_price_impact)
    private val detailMinimumReceived: DetailDescriptionView =
        findViewById(R.id.detail_minimum_received)
    private val detailLiquidityProviderFee: DetailDescriptionView =
        findViewById(R.id.detail_liquidity_provider_fee)
    private val detailBlockchainFee: DetailDescriptionView =
        findViewById(R.id.detail_blockchain_fee)
    private val detailRoute: DetailDescriptionView = findViewById(R.id.detail_route)
    private val detailProvider: DetailDescriptionView = findViewById(R.id.detail_provider)

    private val valueView: AppCompatTextView = findViewById(R.id.value)

    private val textTertiaryColor = context.textTertiaryColor
    private val textPrimaryColor = context.textPrimaryColor

    var isLoading: Boolean
        get() = detailSwapRate.isLoading
        set(value) {
            detailSwapRate.isLoading = value
        }

    var simulate: Simulate? = null
        set(value) {
            if (value != null) {
                field = value
                setDetailsData(value)
                expand()
            } else {
                field = null
                collapse()
            }
        }

    fun setConfirmMode(receiveToken: SwapState.TokenState, simulate: Simulate) {
        isExpanded = true
        isLoading = false
        layoutParams.height = expandedHeight
        requestLayout()
        setTokenState(receiveToken)
        setVisibleDetails(true)
        setDetailsData(simulate)
    }

    init {
        val roundedCorner = context.dimenF(uikit.R.dimen.offsetMedium)
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, roundedCorner)
            }
        }
        clipToOutline = true
    }

    private fun expand() {
        if (isExpanded) {
            return
        }
        isExpanded = true
        invisibleOnCancel = null
        setVisibleDetails(isVisible = true)
        changeSize(expandedHeight)
    }

    private var invisibleOnCancel: (() -> Unit)? = null
    private fun collapse() {
        if (!isExpanded) {
            return
        }
        isExpanded = false
        invisibleOnCancel = {
            setVisibleDetails(isVisible = false)
        }
        changeSize(collapsedHeight) {
            invisibleOnCancel?.invoke()
        }
    }

    private fun setVisibleDetails(isVisible: Boolean) {
        dividerEndReceive.isInvisible = !isVisible
        detailSwapRate.isInvisible = !isVisible
        dividerStartInfo.isInvisible = !isVisible
        detailPriceImpact.isInvisible = !isVisible
        detailMinimumReceived.isInvisible = !isVisible
        detailLiquidityProviderFee.isInvisible = !isVisible
        detailBlockchainFee.isInvisible = !isVisible
        detailRoute.isInvisible = !isVisible
        detailProvider.isInvisible = !isVisible
    }


    private fun setDetailsData(simulate: Simulate?) {
        if (simulate != null) {
            valueView.text = simulate.askUnits
            valueView.setTextColor(textPrimaryColor)
            detailSwapRate.title = simulate.swapRate
            detailPriceImpact.value = simulate.priceImpact
            detailPriceImpact.setHint(onClickListener = {
                onSnackShow?.invoke("The difference between the market price and estimated price due to trade size.")
            })
            detailMinimumReceived.value = simulate.minimumReceived
            detailMinimumReceived.setHint(onClickListener = {
                onSnackShow?.invoke("Your transaction will revert if there is a large, unfavorable price movement before it is confirmed.")
            })
            detailLiquidityProviderFee.value = simulate.liquidityProviderFee
            detailLiquidityProviderFee.setHint(onClickListener = {
                onSnackShow?.invoke("A portion of each trade goes to liquidity providers as a protocol incentive.")
            })
            detailBlockchainFee.value = simulate.blockchainFee
            detailRoute.value = simulate.route

        } else {
            valueView.text = "0"
            valueView.setTextColor(textTertiaryColor)
        }
    }

    private fun changeSize(targetHeight: Int, onEndAnimation: (animator: Animator) -> Unit = { }) {
        changeAnimation?.cancel()
        changeAnimation = null

        changeAnimation = ValueAnimator.ofInt(height, targetHeight).apply {
            addUpdateListener(this@ReceiveSwapView)
            setDuration(SIZE_ANIMATION_DURATION)
            interpolator = DecelerateInterpolator()
            doOnEnd(onEndAnimation)
            start()
        }
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        layoutParams.height = animation.animatedValue as Int
        requestLayout()
    }

    override fun contentViews(): List<View> {
        return super.contentViews().plus(valueView)
    }

    companion object {
        private const val SIZE_ANIMATION_DURATION = 200L
    }
}