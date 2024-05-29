package com.tonapps.tonkeeper.ui.screen.swap.main

import android.content.res.ColorStateList
import android.graphics.Rect
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tonapps.extensions.getParcelableCompat
import com.tonapps.tonkeeper.core.measuredHeightWithVerticalMargins
import com.tonapps.tonkeeper.core.signer.SingerResultContract
import com.tonapps.tonkeeper.core.toDefaultCoinAmount
import com.tonapps.tonkeeper.core.updateInsetPaddingBottom
import com.tonapps.tonkeeper.extensions.buyCoins
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.fragment.send.popup.SelectTokenPopup
import com.tonapps.tonkeeper.ui.screen.swap.assets.AssetPickerScreen
import com.tonapps.tonkeeper.ui.screen.swap.data.AssetEntity
import com.tonapps.tonkeeper.ui.screen.swap.data.FormattedDecimal
import com.tonapps.tonkeeper.ui.screen.swap.data.RemoteAssets
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapAmount
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapConfig
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapEntityDisplayData.Companion.amountInMatchingCurrency
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapOperationError
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapRequest
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapSettings
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapState
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapTarget
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapTransfer
import com.tonapps.tonkeeper.ui.screen.swap.data.isSameToken
import com.tonapps.tonkeeper.ui.screen.swap.main.widget.SimulationDetailsHolder
import com.tonapps.tonkeeper.ui.screen.swap.main.widget.SwapTargetLayout
import com.tonapps.tonkeeper.ui.screen.swap.settings.SwapSettingsScreen
import com.tonapps.tonkeeper.ui.screen.swap.stonfi.Stonfi
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.accentBlueColor
import com.tonapps.uikit.color.accentGreenColor
import com.tonapps.uikit.color.accentRedColor
import com.tonapps.uikit.color.buttonPrimaryBackgroundColor
import com.tonapps.uikit.color.buttonPrimaryBackgroundDisabledColor
import com.tonapps.uikit.color.buttonPrimaryForegroundColor
import com.tonapps.uikit.color.buttonSecondaryBackgroundColor
import com.tonapps.uikit.color.buttonSecondaryBackgroundDisabledColor
import com.tonapps.uikit.color.buttonSecondaryForegroundColor
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletType
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.ton.crypto.base64
import org.ton.crypto.encodeHex
import uikit.ArgbEvaluator
import uikit.HapticHelper
import uikit.animator.BoolAnimator
import uikit.base.BaseFragment
import uikit.dialog.alert.AlertDialog
import uikit.drawable.OverrideBoundsDrawable
import uikit.drawable.RoundRectDrawable
import uikit.effect.FadeAndScaleVisibilityEffect.Companion.applyFadeAndScaleVisibilityAffect
import uikit.extensions.doKeyboardAnimation
import uikit.extensions.focusWithKeyboard
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.hapticConfirm
import uikit.extensions.hapticReject
import uikit.extensions.hideKeyboard
import uikit.extensions.isLtr
import uikit.extensions.isRtl
import uikit.extensions.range
import uikit.extensions.withAlpha
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.AutoDisableNestedScrollView
import uikit.widget.BottomSheetLayout
import uikit.widget.HeaderView
import uikit.widget.LoaderView
import uikit.widget.RotateImageView


class SwapScreen : BaseFragment(R.layout.fragment_swap_main), BaseFragment.BottomSheet {
    companion object {
        fun newInstance(uri: Uri, address: String, walletType: WalletType, sendToken: TokenEntity, receiveToken: TokenEntity? = null): SwapScreen {
            val localId = SwapViewModel.newLocalId()
            val screen = SwapScreen()
            screen.setArgs(SwapArgs(localId, uri, address, walletType, sendToken, receiveToken), ignoreErrors = true)
            return screen
        }

        private var _lastSpeedDebug = 0L
        private fun debugSpeedReset() {
            _lastSpeedDebug = SystemClock.uptimeMillis()
        }
        private fun debugSpeed(what: String) {
            if (!SwapConfig.LOGGING_ENABLED) return
            val newNow = SystemClock.uptimeMillis()
            val elapsed = if (_lastSpeedDebug != 0L) {
                newNow - _lastSpeedDebug
            } else {
                -1L
            }
            _lastSpeedDebug = newNow
            val msg = "[${SwapConfig.debugTimestamp()}] ${elapsed}ms: $what"
            if (elapsed >= 20) {
                Log.e(SwapConfig.LOGGING_TAG, msg)
            } else {
                Log.d(SwapConfig.LOGGING_TAG, msg)
            }
        }
    }

    private val args: SwapArgs by lazy {
        lazyArgs as? SwapArgs ?: SwapArgs(requireArguments())
    }

    private val settings: SettingsRepository by inject()

    private lateinit var headerView: HeaderView
    private lateinit var contentView: AutoDisableNestedScrollView

    private lateinit var sendWrap: SwapTargetLayout
    private lateinit var receiveWrap: SwapTargetLayout
    private lateinit var simulationDetails: SimulationDetailsHolder

    private lateinit var swapButtonView: RotateImageView

    private lateinit var labelAction: ViewGroup
    private lateinit var labelButtonWrap: ViewGroup
    private lateinit var labelButtonProgress: LoaderView
    private lateinit var labelButtonText: AppCompatTextView

    private lateinit var cancelButtonWrap: FrameLayout
    private lateinit var cancelButtonText: AppCompatTextView

    private val labelInProgress: BoolAnimator by lazy {
        BoolAnimator(180L, DecelerateInterpolator()) { _, animatedValue, _, _ ->
            labelButtonText.applyFadeAndScaleVisibilityAffect(1f - animatedValue,
                goneVisibility = View.INVISIBLE,
                goneScale = .95f
            )
            labelButtonProgress.applyFadeAndScaleVisibilityAffect(animatedValue,
                goneScale = 1.0f
            )
        }
    }

    private val labelActiveForegroundColor: Int by lazy {
        requireContext().buttonPrimaryForegroundColor
    }
    private val labelInactiveForegroundColor: Int by lazy {
        requireContext().buttonSecondaryForegroundColor
    }
    private val labelActiveBackgroundColor: Int by lazy {
        requireContext().buttonPrimaryBackgroundColor
    }
    private val labelInactiveBackgroundColor: Int by lazy {
        requireContext().buttonSecondaryBackgroundColor
    }

    private val offsetMedium by lazy {
        labelButtonWrap.context.getDimensionPixelSize(uikit.R.dimen.offsetMedium)
    }
    private val offsetMediumHalf by lazy {
        labelButtonWrap.context.getDimensionPixelSize(uikit.R.dimen.offsetMediumHalf)
    }

    private val confirmButtonClip by lazy { Rect() }

    private val cancelButtonVisible: BoolAnimator by lazy {
        BoolAnimator(220L, DecelerateInterpolator()) { state, animatedValue, stateChanged, prevState ->
            if (stateChanged && (state == BoolAnimator.State.INTERMEDIATE || prevState == BoolAnimator.State.INTERMEDIATE)) {
                val params = ConstraintLayout.LayoutParams(labelButtonWrap.layoutParams as ConstraintLayout.LayoutParams)
                when (state) {
                    BoolAnimator.State.FALSE,
                    BoolAnimator.State.TRUE -> {
                        /*
                          android:layout_marginStart="@dimen/offsetMediumHalf"
                          app:layout_goneMarginStart="@dimen/offsetMedium"
                          app:layout_constraintStart_toEndOf="@id/label_button_cancel_wrap"*/
                        params.marginStart = offsetMediumHalf
                        params.startToEnd = R.id.label_button_cancel_wrap
                        params.startToStart = ConstraintLayout.LayoutParams.UNSET
                    }
                    BoolAnimator.State.INTERMEDIATE -> {
                        /*
                          android:layout_marginStart="@dimen/offsetMedium"
                          app:layout_goneMarginStart="0dp"
                          app:layout_constraintStart_toStartOf="parent"*/
                        params.marginStart = offsetMedium
                        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                        params.startToEnd = ConstraintLayout.LayoutParams.UNSET
                    }
                }
                labelButtonWrap.layoutParams = params
            }

            when (state) {
                BoolAnimator.State.FALSE,
                BoolAnimator.State.TRUE -> {
                    labelButtonText.translationX = 0.0f
                    labelButtonProgress.translationX = 0.0f
                    if (stateChanged) {
                        labelButtonWrap.clipBounds = null
                        val drawable = labelButtonWrap.background
                        if (drawable is OverrideBoundsDrawable) {
                            drawable.setUseOriginalBounds()
                            labelButtonWrap.background = labelRippleDrawable
                        }
                    }
                }
                BoolAnimator.State.INTERMEDIATE -> {
                    val adjustWidth = cancelButtonText.measuredWidth + offsetMedium
                    val x = adjustWidth.toFloat() * animatedValue * 0.5f
                    labelButtonText.translationX = x
                    labelButtonProgress.translationX = x

                    confirmButtonClip.bottom = labelButtonWrap.measuredHeight
                    val clippedWidth: Int = (labelButtonWrap.measuredWidth.toFloat() - adjustWidth.toFloat() * animatedValue).toInt()
                    if (labelButtonWrap.isRtl()) {
                        confirmButtonClip.left = 0
                        confirmButtonClip.right = clippedWidth
                    } else {
                        confirmButtonClip.right = labelButtonWrap.measuredWidth;
                        confirmButtonClip.left = confirmButtonClip.right - clippedWidth
                    }
                    val drawable = labelButtonWrap.background
                    if (drawable is OverrideBoundsDrawable) {
                        drawable.setOverrideBounds(confirmButtonClip)
                    } else {
                        labelButtonWrap.background = OverrideBoundsDrawable(drawable).apply {
                            setOverrideBounds(confirmButtonClip)
                        }
                    }
                    labelButtonWrap.clipBounds = confirmButtonClip
                }
            }

            when (state) {
                BoolAnimator.State.FALSE -> {
                    cancelButtonWrap.visibility = View.GONE
                    cancelButtonText.translationX = 0.0f
                }
                BoolAnimator.State.TRUE -> {
                    cancelButtonWrap.visibility = View.VISIBLE
                    cancelButtonText.translationX = 0.0f
                }
                BoolAnimator.State.INTERMEDIATE -> {
                    val direction = if (cancelButtonWrap.isLtr()) {
                        -1.0f
                    } else {
                        1.0f
                    }
                    cancelButtonWrap.translationX = cancelButtonWrap.measuredWidth.toFloat() * direction * (1.0f - animatedValue)
                    if (stateChanged) {
                        cancelButtonWrap.visibility = View.VISIBLE
                    }
                }
            }

        }
    }

    private val labelRippleSecondaryColor by lazy {
        if (SwapConfig.TRANSPARENT_LABEL_ACTION_RIPPLE) { // see constant comment
            0x40000000
        } else {
            labelButtonWrap.context.buttonSecondaryBackgroundDisabledColor
        }
    }

    private val labelRipplePrimaryColor by lazy {
        if (SwapConfig.TRANSPARENT_LABEL_ACTION_RIPPLE) {
            0x40000000
        } else {
            labelButtonWrap.context.buttonPrimaryBackgroundDisabledColor
        }
    }

    private val labelRippleDrawable: RippleDrawable by lazy {
        val colorStateList = ColorStateList.valueOf(labelRippleSecondaryColor)
        RippleDrawable(colorStateList, LayerDrawable(arrayOf(labelRoundRectBackground)), null)
    }

    private val labelRoundRectBackground: RoundRectDrawable by lazy {
        val cornerRadius = labelButtonWrap.context.getDimensionPixelSize(uikit.R.dimen.cornerMedium).toFloat()
        val drawable = RoundRectDrawable()
        drawable.cornerRadius = cornerRadius
        drawable.color = labelInactiveBackgroundColor
        drawable
    }

    private fun updateLabelButtonBackgroundColor() {
        val color = ArgbEvaluator.instance.evaluate(labelIsActive.floatValue,
            labelInactiveBackgroundColor,
            labelActiveBackgroundColor
        )
        if (labelRoundRectBackground.color != color) {
            labelRoundRectBackground.color = color
            labelButtonWrap.invalidate()
        }
    }

    private val labelIsActive: BoolAnimator by lazy {
        BoolAnimator(220L, DecelerateInterpolator()) { state, animatedValue, stateChanged, _ ->
            if (SwapConfig.ALIGN_BUTTON_BELOW_CONTENT_WHEN_ACTIVE) {
                updateNextButtonPosition()
            }
            if (stateChanged) {
                when (state) {
                    BoolAnimator.State.FALSE -> {
                        val colorStateList = ColorStateList.valueOf(labelRippleSecondaryColor)
                        labelRippleDrawable.setColor(colorStateList)
                    }
                    BoolAnimator.State.TRUE -> {
                        val colorStateList = ColorStateList.valueOf(labelRipplePrimaryColor)
                        labelRippleDrawable.setColor(colorStateList)
                    }
                    BoolAnimator.State.INTERMEDIATE -> { }
                }
            }
            when (state) {
                BoolAnimator.State.FALSE -> {
                    labelButtonText.setTextColor(labelInactiveForegroundColor)
                }
                BoolAnimator.State.TRUE -> {
                    val colorStateList = ContextCompat.getColorStateList(labelButtonText.context, uikit.R.color.button_primary_foreground_selector)
                    labelButtonText.setTextColor(colorStateList)
                }
                BoolAnimator.State.INTERMEDIATE -> {
                    if (labelRippleSecondaryColor != labelRipplePrimaryColor) {
                        labelRippleDrawable.setColor(ColorStateList.valueOf(ArgbEvaluator.instance.evaluate(
                            animatedValue,
                            labelRippleSecondaryColor,
                            labelRipplePrimaryColor
                        )))
                    }
                    labelButtonText.setTextColor(ArgbEvaluator.instance.evaluate(
                        animatedValue,
                        labelInactiveForegroundColor,
                        labelActiveForegroundColor
                    ))
                }
            }
            updateLabelButtonBackgroundColor()
        }
    }

    private val selectTokenPopup: SelectTokenPopup by lazy {
        SelectTokenPopup(requireContext())
    }

    private val swapViewModel: SwapViewModel by viewModel {
        val modelArgs = SwapModelArgs(
            args.localId,
            args.address,
            args.sendToken,
            args.receiveToken,
            args.confirmation?.let { SwapTransfer(it) }
        )
        parametersOf(modelArgs)
    }

    private val currentScope: CoroutineScope
        get() = this.lifecycleScope

    private var behavior: BottomSheetBehavior<FrameLayout>? = null
    override fun onPrepareToShow(parent: BottomSheetLayout, behavior: BottomSheetBehavior<FrameLayout>) {
        this.behavior = behavior
    }

    private fun checkHideable() {
        behavior?.isHideable = !contentView.topScrolled && !criticalOperationInProgress
    }

    @OptIn(FlowPreview::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { hideKeyboardAndRun(::alertOrFinish) }
        if (args.isConfirmation) {
            headerView.setIcon(0)
            headerView.alignTitleToStart()
            headerView.title = getString(Localization.swap_confirm)
        } else {
            headerView.doOnCloseClick = {
                hideKeyboardAndRun(::openSwapSettings)
            }
            headerView.title = getString(Localization.swap)
        }

        contentView = view.findViewById(R.id.content)
        contentView.getChildAt(0).addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateNextButtonPosition()
        }
        contentView.onTopScrolledStateChanged = { hasScrolled ->
            checkHideable()
        }

        sendWrap = view.findViewById(R.id.send_wrap)
        sendWrap.animationsEnabled = this::animationsEnabled
        sendWrap.balanceActionButtonVisible.changeValue(!args.isConfirmation, false)
        sendWrap.doOnBalanceActionClick {
            getCurrentFocus()?.hideKeyboard()
            val send = swapViewModel.swapState.value.send
            var balance = send.balance
            if (SwapConfig.EXCLUDE_GAS_FROM_MAX_AMOUNT) {
                val extraFees = Stonfi.extraFees(send.isTon)
                if (balance > extraFees) {
                    balance -= extraFees
                }
            }
            swapViewModel.setAmount(SwapTarget.SEND, FormattedDecimal(balance, balance.toDefaultCoinAmount(send.token)), SwapAmount.Origin.BALANCE_MAXIMUM)
        }

        receiveWrap = view.findViewById(R.id.receive_wrap)
        receiveWrap.animationsEnabled = this::animationsEnabled

        if (args.isConfirmation) {
            // sendWrap.inConfirmationMode = true
            // receiveWrap.inConfirmationMode = true
        } else {
            sendWrap.setOnTokenClickListener {
                selectToken(it, SwapTarget.SEND)
            }
            sendWrap.setOnTokenLongClickListener { tokenView ->
                @Suppress("KotlinConstantConditions")
                if (!swapViewModel.swapState.value.canBeModified) {
                    false
                } else if (SwapConfig.QUICK_TOKEN_PICKER_ASSET_COUNT_MAX < 0) {
                    val accountTokens = swapViewModel.accountTokens.value
                    swapViewModel.swapState.value.let {
                        if (accountTokens.isNotEmpty()) {
                            openQuickTokenSelector(tokenView, SwapTarget.SEND, it, accountTokens)
                            true
                        } else {
                            false
                        }
                    }
                } else {
                    selectToken(tokenView, SwapTarget.SEND, false)
                    true
                }
            }
            sendWrap.doOnAmountChange { _, amount, formatted ->
                handleAmountChange(SwapTarget.SEND, FormattedDecimal(amount, formatted))
            }

            receiveWrap.setOnTokenClickListener {
                selectToken(it, SwapTarget.RECEIVE)
            }
            receiveWrap.setOnTokenLongClickListener { tokenView ->
                @Suppress("KotlinConstantConditions")
                if (SwapConfig.QUICK_TOKEN_PICKER_ASSET_COUNT_MAX < 0 && swapViewModel.swapState.value.canBeModified) {
                    val accountTokens = swapViewModel.accountTokens.value
                    if (accountTokens.isNotEmpty()) {
                        openQuickTokenSelector(tokenView, SwapTarget.RECEIVE, swapViewModel.swapState.value, accountTokens)
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
            receiveWrap.doOnAmountChange { _, amount, formatted ->
                handleAmountChange(SwapTarget.RECEIVE, FormattedDecimal(amount, formatted))
            }
        }

        simulationDetails = SimulationDetailsHolder(this, receiveWrap,
            hideRate = args.isConfirmation,
            initialHideSimulationDetails = !args.isConfirmation && !settings.swapDetailsOpen,
            animationsEnabled = ::animationsEnabled) {
            updateNextButtonPosition()
        }
        if (!args.isConfirmation) {
            simulationDetails.onAdvancedDetailsHiddenToggle = { areHidden ->
                currentScope.launch(Dispatchers.IO) {
                    // For some reason the first toggle freezes couple frames
                    settings.swapDetailsOpen = !areHidden
                }
            }
        }

        swapButtonView = view.findViewById(R.id.swap)
        if (args.isConfirmation) {
            swapButtonView.visibility = View.GONE
        } else {
            swapButtonView.setOnClickListener {
                getCurrentFocus()?.hideKeyboard()
                swapButtonView.animatedRotation = swapButtonView.finalAnimatedRotation - 180.0f
                swapViewModel.swapTokens()
            }
        }

        labelAction = view.findViewById(R.id.label_action)
        cancelButtonWrap = labelAction.findViewById(R.id.label_button_cancel_wrap)
        cancelButtonText = cancelButtonWrap.findViewById(R.id.label_button_cancel)
        cancelButtonText.setOnClickListener {
            alertOrFinish()
        }
        labelButtonWrap = labelAction.findViewById(R.id.label_button_wrap)
        labelButtonWrap.background = labelRippleDrawable
        labelButtonText = labelButtonWrap.findViewById(R.id.label_button)
        labelButtonProgress = labelButtonWrap.findViewById(R.id.label_progress)
        labelButtonProgress.setTrackColor(labelButtonProgress.getColor().withAlpha(.32f))
        labelButtonProgress.setThickness(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2.5f, labelButtonProgress.context.resources.displayMetrics))
        view.doKeyboardAnimation { offset, progress, isShowing, navigationBarSize ->
            updateNextButtonPosition(offset.toFloat())
            contentView.updateInsetPaddingBottom(offset, progress, isShowing, navigationBarSize, labelAction.measuredHeightWithVerticalMargins)
        }

        currentScope.launch {
            // Bottom buttons flow.
            // Filter out changes that affect the button, and animate it accordingly.
            swapViewModel.swapState.distinctUntilChanged { old, new ->
                old.status == new.status &&
                old.send.isAmountInsufficient == new.send.isAmountInsufficient &&
                (!old.send.isAmountInsufficient || old.send.token.isSameToken(new.send.token)) &&
                (old.status != SwapState.Status.SWAP_FAILED || old.simulationErrorMessage == new.simulationErrorMessage)
            }.collect { state ->
                debugSpeedReset()
                updateLabelButton(state)
                debugSpeed("updateLabelButton")
            }
        }

        // Setup flows, and handle animations smoothly.
        // There are many flows to not trigger rudimentary changes,
        // which could affect animations performance.

        // Simulation details flow (the list below the RECEIVE wrap).
        // Filter out changes that affect simulation details state
        simulationDetails.subscribeToUpdates(swapViewModel.swapState, swapViewModel.requestsCount, currentScope)

        currentScope.launch {
            // Restrict drag down when critical operation is in progress
            swapViewModel.swapState.map { state ->
                state.status == SwapState.Status.SWAP_IN_PROGRESS
            }.distinctUntilChanged().collect { haveCriticalOperation ->
                criticalOperationInProgress = haveCriticalOperation
                checkHideable()
            }
        }

        currentScope.launch {
            // SEND: validate if amount is insufficient
            swapViewModel.swapState.map { state ->
                state.send.isAmountInsufficient
            }.distinctUntilChanged().collect { isAmountInsufficient ->
                sendWrap.isAmountInsufficient = isAmountInsufficient
            }
        }
        currentScope.launch {
            // SEND: current token updates
            swapViewModel.swapState.map { state ->
                state.send.token
            }.distinctUntilChanged { old, new ->
                old.isSameToken(new)
            }.collect { sendToken ->
                sendWrap.token = sendToken
            }
        }
        currentScope.launch {
            // RECEIVE: current token updates
            swapViewModel.swapState.map { state ->
                state.receive.token
            }.distinctUntilChanged { old, new ->
                old.isSameToken(new)
            }.collect { receiveToken ->
                receiveWrap.token = receiveToken
            }
        }
        for (target in SwapTarget.entries) {
            currentScope.launch {
                // SEND: token amount updates
                // RECEIVE: token amount updates
                swapViewModel.swapState.map { state ->
                    val entity = state.takeSwapEntity(target)
                    if (entity.hasToken) {
                        entity.amount
                    } else {
                        null
                    }
                }.distinctUntilChanged { old, new ->
                    (old?.amount?.stringRepresentation ?: "") == (new?.amount?.stringRepresentation ?: "")
                }.collect { newAmount ->
                    ignoreAmountChanges = true
                    when (target) {
                        SwapTarget.SEND -> sendWrap.forceAmount(newAmount)
                        SwapTarget.RECEIVE -> receiveWrap.forceAmount(newAmount)
                    }
                    ignoreAmountChanges = false
                }
            }
        }

        if (args.isConfirmation) {
            currentScope.launch {
                // SEND amount in USD updates
                swapViewModel.swapState.map { state ->
                    state.send.displayData.amountInMatchingCurrency(state.receive.displayData)
                }.distinctUntilChanged().collect { amountInAnyCurrency ->
                    sendWrap.balanceText = amountInAnyCurrency
                }
            }
            currentScope.launch {
                // RECEIVE amount in USD updates
                swapViewModel.swapState.map { state ->
                    state.receive.displayData.amountInMatchingCurrency(state.send.displayData)
                }.distinctUntilChanged().collect { amountInAnyCurrency ->
                    receiveWrap.balanceText = amountInAnyCurrency
                }
            }
            currentScope.launch {
                // Swap transfer errors toasts
                swapViewModel.swapState.map { it.transfer }.distinctUntilChanged().filter { transfer ->
                    transfer?.state == SwapTransfer.State.FAILED
                }.map { transfer -> transfer?.error ?: "" }.debounce(cancelButtonVisible.duration).collect { transferError ->
                    animateFailure(FailureType.SWAP_OPERATION, transferError)
                }
            }
            currentScope.launch {
                // Swap transfer errors toasts
                swapViewModel.swapState.map {
                    it.transfer?.state ?: SwapTransfer.State.WAITING_FOR_USER_CONFIRMATION
                }.distinctUntilChanged { old, new ->
                    old == new || (old.inProgress && new.inProgress)
                }.collect { transferStatus ->
                    when (transferStatus) {
                        SwapTransfer.State.SUCCESSFUL -> {
                            delay(labelIsActive.duration)
                            animateSwapSuccess()
                        }
                        SwapTransfer.State.FAILED -> {
                            // Do nothing. Failures are handled by other flow
                        }
                        SwapTransfer.State.PREPARING,
                        SwapTransfer.State.WAITING_FOR_SIGNATURE,
                        SwapTransfer.State.IN_PROGRESS -> {
                            if (SwapConfig.ENABLE_VIBRATIONS) {
                                labelButtonWrap.hapticConfirm()
                            }
                        }
                        SwapTransfer.State.WAITING_FOR_USER_CONFIRMATION -> { }
                    }
                }
            }
        } else {
            currentScope.launch {
                // Swap (<->) button flow & MAX button enable/disable when in progress
                swapViewModel.swapState.map { state ->
                    state.canBeModified
                }.distinctUntilChanged().collect { canBeModified ->
                    sendWrap.balanceActionButtonEnabled = canBeModified
                    swapButtonView.isEnabled = canBeModified
                }
            }

            currentScope.launch {
                // MAX button flow. Hide it if balance is empty
                swapViewModel.swapState.map { state ->
                    state.send.displayData?.hasAcquiredFunds ?: false
                }.distinctUntilChanged().collect { needMaxButton ->
                    sendWrap.balanceActionButtonVisible.changeValue(needMaxButton, animationsEnabled())
                }
            }
            currentScope.launch {
                // SEND: Enable/disable amount input.
                swapViewModel.swapState.map { state ->
                    state.send.hasToken && state.canBeModified
                }.distinctUntilChanged().collect { canEditSendAmount ->
                    sendWrap.inputEnabled = canEditSendAmount
                }
            }
            currentScope.launch {
                // RECEIVE: Enable/disable amount input
                swapViewModel.swapState.map { state ->
                    state.receive.hasToken && state.canBeModified
                }.distinctUntilChanged().collect { canEditReceiveAmount ->
                    receiveWrap.inputEnabled = canEditReceiveAmount
                }
            }
            currentScope.launch {
                // SEND: balance display.
                swapViewModel.swapState.map { state ->
                    state.send
                }.distinctUntilChanged { old, new ->
                    old.displayData == new.displayData
                }.collect { entity ->
                    val resId: Int
                    val balance: String
                    if (entity.displayData != null) {
                        if (!entity.amount.isEmpty && entity.displayData.hasAcquiredFunds) {
                            balance = entity.displayData.balanceMinusAmount
                            resId = if (entity.displayData.balanceMinusAmountNegative) Localization.swap_missing else Localization.swap_remaining
                        } else {
                            balance = entity.displayData.balance
                            resId = Localization.swap_balance
                        }
                        sendWrap.balanceText = sendWrap.resources.getString(resId, balance)
                    } else {
                        sendWrap.balanceText = ""
                    }
                }
            }
            currentScope.launch {
                // RECEIVE: balance display
                swapViewModel.swapState.map { state ->
                    state.receive.displayData?.balance ?: ""
                }.distinctUntilChanged().collect { balance ->
                    receiveWrap.balanceText = if (balance.isNotEmpty()) {
                        receiveWrap.resources.getString(Localization.swap_balance, balance)
                    } else ""
                }
            }
        }
    }

    private fun updateLabelButton(state: SwapState) {
        var labelEnabled = true
        var labelActive = false
        var labelInProgress = false
        var needCancelButton = false

        val isAmountInsufficient = state.send.isAmountInsufficient
        val status = state.status

        if (isAmountInsufficient) {
            val sendToken = state.send.token!!
            if (sendToken.isTon) {
                labelButtonText.text = getString(Localization.swap_btn_insufficient_buy, sendToken.symbol)
                labelButtonWrap.setOnClickListener {
                    hideKeyboardAndRun {
                        navigation?.buyCoins(args.address, args.walletType, sendToken)
                    }
                }
            } else {
                labelEnabled = false
                labelButtonText.text = getString(Localization.swap_btn_insufficient, sendToken.symbol)
            }
        } else when (status) {
            SwapState.Status.WAITING_FOR_SEND_TOKEN -> {
                labelButtonText.text = getString(Localization.swap_btn_choose_token)
                labelButtonWrap.setOnClickListener {
                    selectToken(sendWrap.visibleTokenView, SwapTarget.SEND, true)
                }
            }
            SwapState.Status.WAITING_FOR_RECEIVE_TOKEN -> {
                labelButtonText.text = getString(Localization.swap_btn_choose_token)
                labelButtonWrap.setOnClickListener {
                    selectToken(receiveWrap.visibleTokenView, SwapTarget.RECEIVE, false)
                }
            }
            SwapState.Status.WAITING_FOR_AMOUNT,
            SwapState.Status.INVALID_SEND_AMOUNT -> {
                labelButtonText.text = if (status == SwapState.Status.WAITING_FOR_AMOUNT) {
                    getString(Localization.swap_btn_enter_amount)
                } else {
                    getString(Localization.swap_btn_invalid_amount)
                }
                labelButtonWrap.setOnClickListener {
                    sendWrap.amountInput.focusWithKeyboard()
                }
            }
            SwapState.Status.INVALID_RECEIVE_AMOUNT -> {
                labelButtonText.text = getString(Localization.swap_btn_invalid_amount)
                labelButtonWrap.setOnClickListener {
                    receiveWrap.amountInput.focusWithKeyboard()
                }
            }
            SwapState.Status.LOADING_ASSETS -> {
                labelButtonText.text = getString(Localization.loading)
            }
            SwapState.Status.FIRST_SIMULATION_IN_PROGRESS -> {
                if (SwapConfig.INITIAL_SIMULATION_PROGRESS_VISUAL) {
                    labelInProgress = true
                } else {
                    labelButtonText.text = getString(Localization.swap_btn_simulating)
                }
                labelEnabled = false
            }
            SwapState.Status.SIMULATION_ERROR -> {
                val errorMessage = state.simulationErrorMessage
                labelButtonText.text = getString(Localization.swap_btn_error_simulation)
                if (errorMessage.isNotEmpty()) {
                    labelButtonWrap.setOnClickListener {
                        currentScope.launch {
                            animateFailure(FailureType.SIMULATION, errorMessage)
                        }
                    }
                } else {
                    labelEnabled = false
                }
            }
            SwapState.Status.PRICE_IMPACT_TOO_HIGH -> {
                labelButtonText.text = getString(Localization.swap_btn_error_price_impact)
                labelEnabled = false
            }
            SwapState.Status.READY_FOR_USER_CONFIRMATION -> {
                labelButtonText.text = getString(Localization.swap_btn_continue)
                labelButtonWrap.setOnClickListener {
                    hideKeyboardAndRun {
                        requestSwapConfirmation(it)
                    }
                }
                labelActive = true
            }
            SwapState.Status.SWAP_READY -> {
                labelButtonText.text = getString(Localization.swap_btn_confirm)
                labelButtonWrap.setOnClickListener {
                    hideKeyboardAndRun {
                        requestSwap(it, false)
                    }
                }
                labelActive = true
                needCancelButton = true
            }
            SwapState.Status.ESTIMATING_BLOCKCHAIN_FEES,
            SwapState.Status.SWAP_IN_PROGRESS -> {
                labelInProgress = true
                labelEnabled = false
            }
            SwapState.Status.SWAP_SUCCESSFUL -> {
                labelActive = true
                labelButtonText.text = getString(Localization.swap_btn_done)
                labelButtonWrap.setOnClickListener {
                    openSwapInExplorer()
                }
            }
            SwapState.Status.SWAP_FAILED -> {
                val canRetry = state.transfer?.canPerformOnBlockchain ?: false
                labelButtonText.text = if (canRetry) {
                    getString(Localization.swap_btn_retry)
                } else {
                    getString(Localization.swap_btn_failed)
                }
                labelActive = canRetry
                needCancelButton = canRetry
                if (canRetry) {
                    labelButtonWrap.setOnClickListener {
                        requestSwap(it, true)
                    }
                } else {
                    labelEnabled = false
                }
            }
        }

        val animationsEnabled = animationsEnabled()

        cancelButtonVisible.changeValue(needCancelButton, animationsEnabled)
        cancelButtonText.isEnabled = needCancelButton

        this.labelButtonWrap.isEnabled = labelEnabled
        this.labelIsActive.changeValue(labelActive, animationsEnabled)
        this.labelInProgress.changeValue(labelInProgress, animationsEnabled)
    }

    private var lastKeyboardOffset: Float = 0.0f

    fun updateNextButtonPosition(keyboardOffset: Float = lastKeyboardOffset) {
        lastKeyboardOffset = keyboardOffset
        if (args.isConfirmation) {
            labelAction.translationY = -keyboardOffset
        } else {
            val content = contentView.getChildAt(0)
            val contentHeight = contentView.paddingTop + content.measuredHeight - receiveWrap.measuredHeight + simulationDetails.animatedHeight
            val alignToContentBottom = contentView.top - labelAction.top + contentHeight
            val activeY = alignToContentBottom.toFloat()
            val defaultY = minOf(-keyboardOffset, activeY)
            if (SwapConfig.ALIGN_BUTTON_BELOW_CONTENT_WHEN_ACTIVE) {
                labelAction.translationY = labelIsActive.floatValue.range(defaultY, activeY)
            } else {
                labelAction.translationY = defaultY
            }
        }
    }

    private var ignoreAmountChanges: Boolean = false

    private fun handleAmountChange (target: SwapTarget, value: FormattedDecimal) {
        if (ignoreAmountChanges) return
        swapViewModel.setAmount(target, value)
    }

    private var selectTokenJob: Job? = null
    private fun selectToken(view: View, target: SwapTarget, allowMinimized: Boolean = target == SwapTarget.SEND) {
        selectTokenJob?.cancel()
        selectTokenJob = currentScope.launch {
            try {
                val data = swapViewModel.loadedData.first()

                val state = data.uiState
                val marketList = data.marketList
                val remoteAssets = data.remoteAssets

                val currentToken = state.takeSwapEntity(target).token
                val reverseEntity = state.takeSwapEntity(target.reverse)
                val reverseToken = reverseEntity.token
                if (marketList.isEmpty()) {
                    navigation?.toast(requireContext().getString(Localization.swap_market_unavailable))
                    return@launch
                }
                var limitToMarkets: Set<String>? = null
                if (target == SwapTarget.RECEIVE && reverseToken != null) {
                    val address = reverseToken.getUserFriendlyAddress(wallet = false, testnet = false)
                    limitToMarkets = marketList[address]
                    if (limitToMarkets.isEmpty()) {
                        navigation?.toast(requireContext().getString(Localization.swap_impossible, reverseToken.symbol))
                        return@launch
                    }
                }
                @Suppress("KotlinConstantConditions")
                if (SwapConfig.QUICK_TOKEN_PICKER_ASSET_COUNT_MAX > 0 && target == SwapTarget.SEND && allowMinimized && reverseToken == null) {
                    val accountTokens = swapViewModel.accountTokens.value
                    if (accountTokens.isNotEmpty() && accountTokens.size <= SwapConfig.QUICK_TOKEN_PICKER_ASSET_COUNT_MAX) {
                        openQuickTokenSelector(view, target, state, accountTokens)
                        return@launch
                    }
                }
                openFullTokenSelector(view, target, state, remoteAssets, limitToMarkets)
            } finally {
                selectTokenJob = null
            }
        }
    }

    private fun openFullTokenSelector(view: View, target: SwapTarget, state: SwapState, remoteAssets: RemoteAssets, limitToMarkets: Set<String>?) {
        if (remoteAssets.isEmpty() || !state.canBeModified) return
        hideKeyboardAndRun {
            val pickerRequestKey = "asset_${target}_${args.localId}"
            setFragmentResultListener(pickerRequestKey) { _, bundle ->
                val newAsset: AssetEntity? = bundle.getParcelableCompat(AssetPickerScreen.RESULT_KEY)
                newAsset?.let {
                    swapViewModel.selectToken(target, it)
                }
            }

            val entity = state.takeSwapEntity(target)
            val listScreen = AssetPickerScreen.newInstance(pickerRequestKey, remoteAssets, target, limitToMarkets, entity.token)
            navigation?.add(listScreen)
        }
    }

    private fun openQuickTokenSelector(view: View, target: SwapTarget, state: SwapState, tokens: List<AccountTokenEntity>) {
        if (tokens.isEmpty() || !state.canBeModified) return

        val entity = state.takeSwapEntity(target)
        val selectedToken = tokens.firstOrNull {
            it.address == entity.tokenAddress
        }

        selectTokenPopup.tokens = tokens
        selectTokenPopup.selectedToken = selectedToken
        selectTokenPopup.doOnSelectJetton = { token ->
            swapViewModel.selectToken(target, token)
        }
        selectTokenPopup.show(view)
    }

    private fun requestSwapConfirmation(view: View) {
        currentScope.launch {
            val request = swapViewModel.requestSwapConfirmation()
            if (request != null) {
                openSwapConfirmation(request)
            }
        }
    }

    private fun openSwapConfirmation(request: SwapRequest, delay: Long = labelIsActive.duration) {
        currentScope.launch {
            val operationDetails = request.operationDetails!!
            if (operationDetails.hasError) {
                val error = operationDetails.error!!
                val errorMessage = operationDetails.errorMessage!!
                val shortDelay = (delay * 0.3f).toLong()
                if (shortDelay > 0) {
                    delay(shortDelay)
                }
                when (error) {
                    SwapOperationError.CURRENCY_RATE_UPDATE_FAILED -> {
                        animateFailure(FailureType.CURRENCY_RATE_UPDATE, errorMessage) {
                            openSwapConfirmation(request.withoutError(), AlertDialog.DISMISS_DURATION)
                        }
                    }
                    SwapOperationError.LOW_BALANCE_MAY_FAIL -> {
                        animateFailure(FailureType.LOW_BALANCE, errorMessage) {
                            openSwapConfirmation(request.withoutError(), AlertDialog.DISMISS_DURATION)
                        }
                    }
                    SwapOperationError.SIMULATION_FAILED -> {
                        animateFailure(FailureType.SIMULATION, operationDetails.errorMessage)
                    }
                }
            } else if (operationDetails.canAttempt) {
                val args = SwapArgs(
                    args.localId,
                    args.uri,
                    args.address,
                    args.walletType,
                    request.sendAsset.token,
                    request.receiveAsset.token,
                    request
                )
                val screen = SwapScreen().apply { setArgs(args, ignoreErrors = true) }
                if (delay > 0) {
                    delay(delay)
                }
                navigation?.add(screen)
            }
        }
    }

    private fun requestSwap(view: View, isRetry: Boolean) {
        currentScope.launch {
            val transfer = swapViewModel.startSwap(view.context, cancelButtonVisible.duration) ?: return@launch
            if (transfer.state != SwapTransfer.State.WAITING_FOR_SIGNATURE) {
                return@launch
            }

            try {
                signerLauncher.launch(transfer.signerInput)
            } catch (e: Exception) {
                swapViewModel.failTransfer(transfer, "Failed to launch signer: ${e.message}")
            }
        }
    }

    private fun openSwapInExplorer() {
        currentScope.launch {
            val cell = swapViewModel.swapState.map {
                it.transfer?.takeIf { it.state == SwapTransfer.State.SUCCESSFUL }?.success
            }.firstOrNull()
            if (cell != null) {
                val hex = cell.hash().encodeHex()
                navigation?.openURL("https://tonviewer.com/transaction/${hex}", true)
            }
        }

    }

    fun showExplanation(title: CharSequence, hint: CharSequence) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        builder.setColoredButtons()
        builder.setMessage(hint)
        builder.setPositiveButton(Localization.swap_hint_ok)
        builder.show()
    }

    private fun animateSwapSuccess() {
        navigation?.removePreviousScreen()
        if (SwapConfig.ENABLE_VIBRATIONS) {
            HapticHelper.success(labelButtonWrap.context)
        }
        headerView.title = getString(Localization.swap_sent_title)
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(Localization.swap_sent_title)
        builder.setAlertModifier { titleView, positiveButton, negativeButton ->
            titleView.setTextColor(negativeButton.context.accentGreenColor)
            positiveButton.setTextColor(positiveButton.context.accentBlueColor)
        }
        builder.setMessage(Localization.swap_sent_description)
        builder.setPositiveButton(Localization.swap_sent_ok)
        builder.show()
    }

    private enum class FailureType {
        SIMULATION,
        CURRENCY_RATE_UPDATE,
        LOW_BALANCE,
        SWAP_OPERATION
    }

    private suspend fun animateFailure(type: FailureType, message: String, action: ((dialog: AlertDialog) -> Unit)? = null) {
        if (SwapConfig.ENABLE_VIBRATIONS) {
            labelButtonWrap.hapticReject()
        }
        val builder = AlertDialog.Builder(requireContext())
        builder.setAlertModifier { titleView, positiveButton, negativeButton ->
            titleView.setTextColor(negativeButton.context.accentRedColor)
            positiveButton.setTextColor(positiveButton.context.accentBlueColor)
            negativeButton.setTextColor(positiveButton.context.accentBlueColor)
        }
        when (type) {
            FailureType.SIMULATION -> {
                builder.setTitle(Localization.swap_failed_simulation_title)
                builder.setMessage(message)
                builder.setPositiveButton(Localization.swap_failed_simulation_ok, action)
            }
            FailureType.LOW_BALANCE -> {
                builder.setTitle(getString(Localization.swap_failed_balance_title, message))
                builder.setMessage(Localization.swap_failed_balance_description)
                builder.setPositiveButton(Localization.swap_failed_balance_cancel)
                builder.setNegativeButton(Localization.swap_failed_balance_ok, action!!)
            }
            FailureType.CURRENCY_RATE_UPDATE -> {
                val currency = swapViewModel.getWalletCurrency()
                builder.setTitle(Localization.swap_failed_rate_title)
                builder.setMessage(getString(Localization.swap_failed_rate_description, currency.code, message))
                builder.setPositiveButton(Localization.swap_failed_rate_cancel)
                builder.setNegativeButton(Localization.swap_failed_rate_ok, action!!)
            }
            FailureType.SWAP_OPERATION -> {
                builder.setTitle(Localization.swap_failed_operation_title)
                builder.setMessage(message)
                builder.setPositiveButton(Localization.swap_failed_operation_ok, action)
            }
        }
        builder.show()
    }

    override fun onBackPressed(): Boolean {
        alertOrFinish()
        return false
    }

    private fun openSwapSettings() {
        val settingsRequestKey = "swap_settings_${args.localId}"
        setFragmentResultListener(settingsRequestKey) { _, bundle ->
            val newSettings: SwapSettings? = bundle.getParcelableCompat(SwapSettingsScreen.RESULT_KEY)
            newSettings?.let {
                swapViewModel.setSettings(it)
            }
        }
        val currentSettings = swapViewModel.swapState.value.settings
        val screen = SwapSettingsScreen.newInstance(settingsRequestKey, currentSettings)
        navigation?.add(screen)
    }

    private var criticalOperationInProgress: Boolean = false
    private fun alertOrFinish(allowAlert: Boolean = true) {
        if (criticalOperationInProgress && allowAlert) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(Localization.swap_out_title)
            builder.setMessage(Localization.swap_out_description)
            builder.setNegativeButton(Localization.swap_out_ok) {
                alertOrFinish(false)
            }
            builder.setPositiveButton(Localization.cancel)
            builder.setColoredButtons()
            builder.show()
            return
        }
        finish()
    }

    // Signer

    private val signerLauncher = registerForActivityResult(SingerResultContract()) {
        val signature = it?.let { base64(it) }
        currentScope.launch {
            try {
                swapViewModel.continueSwap(signature)
            } catch (e: Exception) {
                swapViewModel.failCurrentTransfer("Cannot request signature: ${e.message}")
            }
        }
    }

    //

    override fun onDragging() {
        super.onDragging()
        getCurrentFocus()?.hideKeyboard()
    }

    private var releaseAnimationsLock: Boolean = false

    private fun animationsEnabled(): Boolean {
        // TODO: return false when other screen overlayed current fully
        return isVisible && isResumed && releaseAnimationsLock
    }

    override fun onStartShowingAnimation() {
        if (SwapConfig.ENABLE_VIBRATIONS && args.isConfirmation) {
            HapticHelper.selection(labelButtonText.context)
        }
        releaseAnimationsLock = true
    }

    override fun onEndShowingAnimation() {
        releaseAnimationsLock = true
    }
}