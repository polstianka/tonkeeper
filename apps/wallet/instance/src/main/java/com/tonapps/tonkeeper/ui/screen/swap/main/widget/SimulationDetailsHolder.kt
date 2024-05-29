package com.tonapps.tonkeeper.ui.screen.swap.main.widget

import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import com.tonapps.tonkeeper.ui.screen.swap.data.PriceImpactGrade
import com.tonapps.tonkeeper.ui.screen.swap.data.SimulationDisplayData
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapConfig
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapState
import com.tonapps.tonkeeper.ui.screen.swap.main.SwapScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.iconSecondaryColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import uikit.animator.BoolAnimator
import uikit.animator.FloatAnimator
import uikit.drawable.ClippedDrawable
import uikit.drawable.OverrideBoundsDrawable
import uikit.extensions.hapticConfirm
import uikit.extensions.updateVisibility
import uikit.extensions.withAlpha
import uikit.widget.DividerView
import uikit.widget.InfoRowView
import uikit.widget.LoaderView
import uikit.widget.RotateImageView

class SimulationDetailsHolder(
    val context: SwapScreen,
    val parent: ViewGroup,
    private val hideRate: Boolean = false,
    initialHideSimulationDetails: Boolean = !hideRate,
    val animationsEnabled: () -> Boolean,
    val onHeightChange: (newHeight: Int) -> Unit
) {
    private val topDivider: DividerView
    private val rate: InfoRowView
    private val progressView: LoaderView
    private val arrowView: RotateImageView

    private val advancedDetailsDivider: DividerView
    private val advancedDetails: ViewGroup
    private val priceImpact: InfoRowView
    private val minimumReceived: InfoRowView
    private val liquidityProviderFee: InfoRowView
    private val blockchainFees: InfoRowView
    private val route: InfoRowView
    private val provider: InfoRowView

    val background: OverrideBoundsDrawable

    companion object {
        const val ARROW_DETAILS_HIDDEN_DEGREES = 0.0f
        const val ARROW_DETAILS_SHOWN_DEGREES = 180.0f
    }

    var onAdvancedDetailsHiddenToggle: (Boolean) -> Unit = { }

    init {
        topDivider = parent.findViewById(R.id.simulation_divider_top)
        rate = parent.findViewById(R.id.simulation_rate)
        progressView = parent.findViewById(R.id.simulation_progress)
        arrowView = parent.findViewById(R.id.simulation_arrow)
        if (!hideRate) {
            arrowView.forcedRotation = if (initialHideSimulationDetails) {
                ARROW_DETAILS_HIDDEN_DEGREES
            } else {
                ARROW_DETAILS_SHOWN_DEGREES
            }
            rate.background = ClippedDrawable(rate.context, uikit.R.drawable.bg_button_spacial)
            rate.setOnClickListener {
                val newValue = !advancedDetailsHidden
                advancedDetailsHidden = newValue
                onAdvancedDetailsHiddenToggle(newValue)
            }
            progressView.setTrackColor(progressView.context.iconSecondaryColor.withAlpha(.32f))
        }

        background = OverrideBoundsDrawable(parent.background)
        parent.background = background

        parent.addOnLayoutChangeListener { v, left, top, right, bottom,
            oldLeft, oldTop, oldRight, oldBottom ->
            val measuredWidth = parent.measuredWidth
            if (clipRect.right != measuredWidth) {
                clipRect.right = measuredWidth
                animateCurrentHeight(false)
            } else {
                animateCurrentHeight(true)
            }
        }

        advancedDetailsDivider = parent.findViewById(R.id.simulation_divider_middle)
        advancedDetails = parent.findViewById(R.id.simulation_advancedDetails)
        for (index in 0 until advancedDetails.childCount) {
            val child = advancedDetails.getChildAt(index)
            if (child is InfoRowView && child.hint.isNotEmpty()) {
                child.background = ClippedDrawable(child.context, uikit.R.drawable.bg_button_spacial)
                child.setOnClickListener {
                    it as InfoRowView
                    it.hapticConfirm()
                    context.showExplanation(it.title, it.hint)
                }
            }
        }

        priceImpact = advancedDetails.findViewById(R.id.simulation_priceImpact)
        minimumReceived = advancedDetails.findViewById(R.id.simulation_minReceived)
        liquidityProviderFee = advancedDetails.findViewById(R.id.simulation_liquidityProviderFee)
        blockchainFees = advancedDetails.findViewById(R.id.simulation_blockchainFees)
        route = advancedDetails.findViewById(R.id.simulation_route)
        provider = advancedDetails.findViewById(R.id.simulation_provider)
    }

    private val progressAnimator by lazy {
        BoolAnimator(180L, DecelerateInterpolator()) { state, animatedValue, stateChanged, prevState ->
            if (stateChanged) {
                val visibility = rate.visibility
                when (state) {
                    BoolAnimator.State.FALSE -> {
                        arrowView.updateVisibility(visibility)
                        progressView.updateVisibility(View.GONE)
                    }
                    BoolAnimator.State.INTERMEDIATE -> {
                        arrowView.updateVisibility(visibility)
                        progressView.updateVisibility(visibility)
                    }
                    BoolAnimator.State.TRUE -> {
                        arrowView.updateVisibility(View.GONE)
                        progressView.updateVisibility(visibility)
                    }
                }
            }
            arrowView.alpha = 1.0f - animatedValue
            progressView.alpha = animatedValue
            // arrowView.applyFadeAndScaleVisibilityAffect(1.0f - animatedValue, updateVisibility = false)
            // progressView.applyFadeAndScaleVisibilityAffect(animatedValue, updateVisibility = false)
        }
    }

    private fun setAdvancedDetailsVisibility(visibility: Int): Boolean {
        var res = advancedDetails.updateVisibility(visibility)
        res = advancedDetailsDivider.updateVisibility(visibility) || res
        return res
    }

    private fun setRateVisibility(visibility: Int): Boolean {
        if (!hideRate) {
            var res = rate.updateVisibility(visibility)
            res = topDivider.updateVisibility(visibility) || res
            if (visibility == View.VISIBLE) {
                val arrowVisibility: Int
                val progressVisibility: Int
                when (progressAnimator.state) {
                    BoolAnimator.State.FALSE -> {
                        arrowVisibility = visibility
                        progressVisibility = View.GONE
                    }
                    BoolAnimator.State.INTERMEDIATE -> {
                        arrowVisibility = visibility
                        progressVisibility = visibility
                    }
                    BoolAnimator.State.TRUE -> {
                        arrowVisibility = View.GONE
                        progressVisibility = visibility
                    }
                }
                res = arrowView.updateVisibility(arrowVisibility) || res
                res = progressView.updateVisibility(progressVisibility) || res
            } else {
                res = arrowView.updateVisibility(visibility) || res
                res = progressView.updateVisibility(visibility) || res
            }
            return res
        }
        return false
    }

    private val clipRect: Rect by lazy {
        Rect()
    }

    var animatedHeight: Int = 0
        get() = clipRect.bottom
        private set(newAnimatedHeight) {
            if (field == newAnimatedHeight) return
            field = newAnimatedHeight
            clipRect.bottom = newAnimatedHeight
            background.setOverrideBounds(clipRect)
            parent.clipBounds = clipRect
            onHeightChange(newAnimatedHeight)
        }

    private fun hideRudimentaryViews(animatedHeight: Float, byAnimationEnd: Boolean) {
        val advancedDetailsHeight = advancedDetails.visibleHeight() + advancedDetailsDivider.visibleHeight()
        val rateHeight = rate.visibleHeight() + topDivider.visibleHeight()
        if ((!isVisible || advancedDetailsHidden) && advancedDetails.visibility != View.GONE) {
            if (animatedHeight <= parent.measuredHeight - advancedDetailsHeight) {
                setAdvancedDetailsVisibility(View.GONE)
            }
        }
        if ((!isVisible || hideRate) && rate.visibility != View.GONE) {
            if (animatedHeight <= parent.measuredHeight - advancedDetailsHeight - rateHeight) {
                setRateVisibility(View.GONE)
            }
        }
    }

    private val heightAnimator = FloatAnimator(220L, DecelerateInterpolator(), onAnimationsFinished = ::hideRudimentaryViews) { animatedHeight ->
        this.animatedHeight = animatedHeight.toInt()
    }

    private fun View.visibleHeight() = if (visibility != View.GONE) measuredHeight else 0

    private fun animateCurrentHeight(allowAnimation: Boolean) {
        var targetHeight = parent.measuredHeight
        if (!isVisible) {
            targetHeight -= topDivider.visibleHeight()
            targetHeight -= rate.visibleHeight()
        }
        if (!isVisible || advancedDetailsHidden) {
            targetHeight -= advancedDetailsDivider.visibleHeight()
            targetHeight -= advancedDetails.visibleHeight()
        }
        heightAnimator.changeValue(targetHeight.toFloat(), allowAnimation && animationsEnabled())
    }

    private var advancedDetailsHidden: Boolean = initialHideSimulationDetails
        set(value) {
            if (field == value) return
            field = value
            arrowView.animatedRotation = if (value) ARROW_DETAILS_HIDDEN_DEGREES else ARROW_DETAILS_SHOWN_DEGREES
            checkChangesAndAnimated(true)
        }

    private fun allowProgressAnimation(): Boolean {
        // Do not animate arrow <-> progress if they are below clipping area
        if (rate.visibility == View.GONE) {
            return false
        }
        if (progressView.visibility == View.GONE && arrowView.visibility == View.GONE) {
            return false
        }
        if (maxOf(progressView.measuredWidth, arrowView.measuredWidth) == 0) {
            return false
        }
        val top = maxOf(progressView.top, arrowView.top)
        if (top == 0 || animatedHeight < top) {
            return false
        }
        return true
    }

    private var isVisible: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            checkChangesAndAnimated(true)
        }

    private fun checkChangesAndAnimated(allowAnimation: Boolean) {
        val animated = allowAnimation && animationsEnabled()

        val rateVisibility = if (isVisible && !hideRate) View.VISIBLE else View.GONE
        val advancedDetailsVisibility = if (isVisible && !advancedDetailsHidden) View.VISIBLE else View.GONE

        var layoutExpected = false
        if (rateVisibility == View.VISIBLE || !animated) {
            layoutExpected = setRateVisibility(rateVisibility)
        }
        if (advancedDetailsVisibility == View.VISIBLE || !animated) {
            layoutExpected = setAdvancedDetailsVisibility(advancedDetailsVisibility) || layoutExpected
        }
        if (!layoutExpected) {
            animateCurrentHeight(true)
        }
    }

    @OptIn(FlowPreview::class)
    fun subscribeToUpdates(flow: StateFlow<SwapState>, requestsFlow: StateFlow<Int>, scope: CoroutineScope) {
        val onError: (String, Throwable) -> Unit = { msg, t ->
            // stack trace is not recoverable even in debug build
            Log.e(SwapConfig.LOGGING_TAG, "ERROR: $msg", t)
            error(msg)
        }
        scope.launch {
            flow.map {
                it.hasVisibleSimulation
            }.catch { onError("206", it) }.distinctUntilChanged().collect { hasSimulation ->
                isVisible = hasSimulation
            }
        }
        if (!hideRate) {
            val inProgressFlow: Flow<Boolean> = combine(flow, requestsFlow) { a, requestsCount ->
                a.canRunSimulations && a.hasVisibleSimulation && (requestsCount > 0 || a.requiresSimulationUpdate)
            }.distinctUntilChanged().debounce {
                if (it) 0L else progressAnimator.duration * 2
            }
            scope.launch {
                inProgressFlow.collect { inProgress ->
                    progressAnimator.changeValue(inProgress, allowProgressAnimation() && animationsEnabled())
                }
            }
            scope.launch {
                flow.filter {
                    it.hasVisibleSimulation
                }.catch { onError("236", it) }.map {
                    it.visibleSimulation!!.swapRate
                }.catch { onError("238", it) }.distinctUntilChanged().collect { swapRate ->
                    rate.title = swapRate
                }
            }
        }
        scope.launch {
            flow.filter {
                it.simulation.isSuccessful
            }.catch { onError("213", it) }.map {
                it.simulation.data!!.priceImpactGrade
            }.catch { onError("215", it) }.distinctUntilChanged().collect { priceImpactGrade ->
                val priceImpactStyle = when (priceImpactGrade) {
                    PriceImpactGrade.MEDIUM -> InfoRowView.Style.WARNING
                    PriceImpactGrade.LOW -> InfoRowView.Style.POSITIVE
                    PriceImpactGrade.HIGH -> InfoRowView.Style.ERROR
                }
                if (!hideRate) {
                    if (priceImpactGrade != PriceImpactGrade.LOW) {
                        rate.titleStyle = priceImpactStyle
                        rate.titleIcon = R.drawable.ic_warning_16
                    } else {
                        rate.titleStyle = InfoRowView.Style.NORMAL
                        rate.titleIcon = 0
                    }
                }
                priceImpact.valueStyle = priceImpactStyle
            }
        }
        val pairs = listOf<Pair<InfoRowView, (SimulationDisplayData) -> String>>(
            Pair(priceImpact) { it.priceImpactPercentage },
            Pair(minimumReceived) { it.minReceived },
            Pair(liquidityProviderFee) { it.fee },
            Pair(blockchainFees) { it.blockchainFee },
            Pair(route) { it.route }
        )
        for (pair in pairs) {
            scope.launch {
                flow.filter {
                    it.hasVisibleSimulation
                }.catch { onError("253", it) }.map {
                    pair.second(it.visibleSimulation!!)
                }.catch { onError("255", it) }.distinctUntilChanged().collect { text ->
                    pair.first.value = text
                }
            }
        }
        scope.launch {
            flow.filter {
                it.simulation.isSuccessful
            }.catch { onError("263", it) }.map {
                it.simulation.data!!.providerName
            }.catch { onError("265", it) }.collect { providerName ->
                provider.visibility = if (providerName.isEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
                provider.value = providerName
            }
        }
    }
}
