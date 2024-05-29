package com.tonapps.tonkeeper.ui.screen.swap.amount

import android.animation.Animator
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.api.swap.StonfiSwapAsset
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.ui.screen.swap.SwapChooseView
import com.tonapps.tonkeeper.ui.screen.swap.SwapScreenEffect
import com.tonapps.tonkeeper.ui.screen.swap.SwapSimulationView
import com.tonapps.tonkeeper.ui.screen.swap.pager.PagerScreen
import com.tonapps.tonkeeper.ui.screen.swap.pager.SwapScreenAdapter
import com.tonapps.tonkeeper.ui.screen.swap.viewSwitcher
import com.tonapps.tonkeeper.ui.screen.swap.visibilityHeightAnimated
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.constantRedColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.dp
import uikit.extensions.focusWithKeyboard
import uikit.extensions.hideKeyboard
import uikit.extensions.scale
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.LoaderView

class SwapAmountScreen : PagerScreen<SwapAmountScreenState, SwapAmountScreenEffect, SwapAmountScreenFeature>(R.layout.fragment_swap_amount), BaseFragment.CustomBackground {

    companion object {
        fun newInstance() = SwapAmountScreen()
    }

    override val feature: SwapAmountScreenFeature by viewModel()

    private lateinit var tokenView: SwapChooseView
    private lateinit var tokenViewRec: SwapChooseView
    private lateinit var valueView: AmountInput
    private lateinit var valueRecView: AmountInput
    private lateinit var availableView: AppCompatTextView
    private lateinit var availableViewRec: AppCompatTextView
    private lateinit var maxButton: AppCompatTextView

    private lateinit var hintButton: FrameLayout
    private lateinit var hintText: AppCompatTextView
    private lateinit var simulationView: SwapSimulationView
    private lateinit var loader: LoaderView
    private lateinit var loader2: LoaderView
    private lateinit var settings: View
    private lateinit var close: View
    private lateinit var skeleton: View
    private lateinit var mainContent: View
    private lateinit var flip: View
    private lateinit var rate: View
    private lateinit var rateText: AppCompatTextView
    private lateinit var secondDivider: View
    private lateinit var expand: View
    private var changingState = false
    private val handler = Handler()
    private var pendingChangeValue = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rate = view.findViewById(R.id.rate)
        rateText = view.findViewById(R.id.rate_text)
        secondDivider = view.findViewById(R.id.second_divider)
        expand = view.findViewById(R.id.expand)
        flip = view.findViewById(R.id.flip)
        flip.setOnClickListener {
            flip.animate().cancel()
            flip.animate().rotation(if (flip.rotation != 180f) 180f else 0f).setDuration(200)
                .start()
            swapFeature.flip()
        }
        skeleton = view.findViewById(R.id.skeleton)
        mainContent = view.findViewById(R.id.main_content)
        settings = view.findViewById(R.id.settings)
        close = view.findViewById(R.id.close)
        settings.setOnClickListener {
            swapFeature.setCurrentPage(SwapScreenAdapter.POSITION_SETTINGS)
        }
        close.setOnClickListener {
            swapFeature.sendEffect(SwapScreenEffect.Finish)
        }
        expand.setOnClickListener {
            expand.animate().cancel()
            expand.animate().rotation(if (simulationView.isVisible) 0f else 180f).setDuration(200)
                .start()
            if (!simulationView.isVisible) {
                valueView.hideKeyboard()
            }
            secondDivider.isVisible = !simulationView.isVisible
            simulationView.visibilityHeightAnimated(!simulationView.isVisible, 210.dp.toFloat())
        }
        simulationView = view.findViewById(R.id.simulation)
        simulationView.setHintListener {
            navigation?.toast(it)
        }
        tokenView = view.findViewById(R.id.token)
        tokenView.setOnClickListener {
            swapFeature.setCurrentFrom(true)
            swapFeature.setCurrentPage(SwapScreenAdapter.POSITION_CHOOSE)
        }

        tokenViewRec = view.findViewById(R.id.token_rec)
        tokenViewRec.setOnClickListener {
            swapFeature.setCurrentFrom(false)
            swapFeature.setCurrentPage(SwapScreenAdapter.POSITION_CHOOSE)
        }

        val setValueTask = Runnable {
            pendingChangeValue = false
            feature.setValue(getValue(), valueView.text.toString())
        }

        valueView = view.findViewById(R.id.value)
        valueView.doAfterTextChanged {
            if (!changingState) {
                pendingChangeValue = true
                handler.removeCallbacks(setValueTask)
                handler.postDelayed(setValueTask, 300)
            }
        }
        valueView.gravity = Gravity.CENTER_VERTICAL or Gravity.END

        valueRecView = view.findViewById(R.id.value_rec)
        valueRecView.doOnTextChanged { _, _, _, _ ->
            if (!changingState) {
                feature.setValueRec(getValueRec(), valueRecView.text.toString())
            }
        }
        valueRecView.gravity = Gravity.CENTER_VERTICAL or Gravity.END

        availableView = view.findViewById(R.id.available)
        availableViewRec = view.findViewById(R.id.available_rec)

        maxButton = view.findViewById(R.id.max)
        maxButton.setOnClickListener {
            if (maxButton.isActivated) {
                clearValue()
            } else {
                setMaxValue()
            }
        }

        hintButton = view.findViewById(R.id.button_hint)
        hintText = view.findViewById(R.id.hint)
        loader = view.findViewById(R.id.loader)
        loader.setSize(2.dp.toFloat())
        loader2 = view.findViewById(R.id.loader2)
        loader2.setSize(1.dp.toFloat())

        hintButton.setOnClickListener {
            feature.uiState.value.simulateData?.let {
                if (feature.uiState.value.canContinue) {
                    swapFeature.setSimulation(it)
                    swapFeature.setAmount(valueView.text.toString())
                    swapFeature.setAmountRec(valueRecView.text.toString())
                    swapFeature.setSettings(
                        feature.uiState.value.slippage,
                        feature.uiState.value.expertMode
                    )
                    swapFeature.setCurrentPage(SwapScreenAdapter.POSITION_CONFIRM)
                } else {
                    hintButton.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.shake
                        )
                    )

                    if (feature.uiState.value.slippage < (feature.uiState.value.simulateData?.priceImpact?.toFloatOrNull()
                            ?: 0f)
                    ) {
                        navigation?.toast(Localization.slippage_high)
                    }
                }
            }

            if (feature.uiState.value.swapFrom == null) {
                swapFeature.setCurrentFrom(true)
                swapFeature.setCurrentPage(SwapScreenAdapter.POSITION_CHOOSE)
            }
        }

        swapFeature.swap.observe(viewLifecycleOwner) { swap ->
            if (swapFeature.swap.value?.currentFrom == true || swapFeature.swap.value?.flip == true) {
                if (feature.uiState.value.swapFrom?.symbol != swap.from?.symbol) {
                    tokenView.animate().cancel()
                    tokenView.animate().scale(1.2f).setDuration(200).withEndAction {
                        tokenView.animate().scale(1f).setDuration(200)
                    }
                }
            }
            if (swapFeature.swap.value?.currentFrom == false || swapFeature.swap.value?.flip == true) {
                if (feature.uiState.value.swapTo?.symbol != swap.to?.symbol) {
                    tokenViewRec.animate().cancel()
                    tokenViewRec.animate().scale(1.2f).setDuration(200).withEndAction {
                        tokenViewRec.animate().scale(1f).setDuration(200)
                    }
                }
            }

            feature.setFromTo(swap.from, swap.to, swap.flip)
            swapFeature.setFlipping(false)
        }
    }

    private fun forceSetAmount(amount: Float) {
        val text = if (0f >= amount) {
            ""
        } else {
            amount.toString()
        }
        val editable = valueView.text ?: return
        editable.replace(0, editable.length, text)
    }

    private fun getValue(): Float {
        val text = Coin.prepareValue(valueView.text.toString())
        return text.toFloatOrNull() ?: 0f
    }

    private fun getValueRec(): Float {
        val text = Coin.prepareValue(valueRecView.text.toString())
        return text.toFloatOrNull() ?: 0f
    }

    private fun setMaxValue() {
        val maxValue = feature.currentBalance
        val text = valueView.text ?: return
        val format = CurrencyFormatter.format(value = maxValue, decimals = feature.decimals)
        text.replace(0, text.length, format)
    }

    private fun clearValue() {
        forceSetAmount(0f)
    }

    private fun setFrom(asset: StonfiSwapAsset?) {
        if (asset != null) {
            tokenView.setData(SwapChooseView.SwapItem.Asset(asset.symbol, asset.imageURL))
        } else {
            tokenView.setData(SwapChooseView.SwapItem.Hint())
        }
    }

    private fun setTo(asset: StonfiSwapAsset?) {
        if (asset != null) {
            tokenViewRec.setData(SwapChooseView.SwapItem.Asset(asset.symbol, asset.imageURL))
        } else {
            tokenViewRec.setData(SwapChooseView.SwapItem.Hint())
        }
    }



    override fun newUiState(state: SwapAmountScreenState) {
        if (state.initialLoading) {
            if (!skeleton.isVisible) skeleton.isVisible = true
            if (mainContent.isVisible) mainContent.isVisible = false
            if (settings.visibility == View.VISIBLE) settings.visibility = View.INVISIBLE
            if (hintButton.isVisible) hintButton.isVisible = false
            if (flip.isVisible) flip.isVisible = false
            return
        }
        changingState = true
        if (skeleton.isVisible) {
            skeleton.isVisible = false
        }
        if (!mainContent.isVisible) {
            mainContent.isVisible = true
            mainContent.alpha = 0.5f
            mainContent.animate().cancel()
            mainContent.animate().alpha(1f).setDuration(200).start()
        }
        if (settings.visibility != View.VISIBLE) {
            settings.visibility = View.VISIBLE
            settings.alpha = 0f
            settings.scale = 0.5f
            settings.animate().cancel()
            settings.animate().alpha(1f).scale(1f).setDuration(200).start()
        }
        if (!hintButton.isVisible) {
            hintButton.isVisible = true
            hintButton.alpha = 0f
            hintButton.animate().cancel()
            hintButton.animate().alpha(1f).setDuration(200).start()
        }
        if (!flip.isVisible) {
            flip.isVisible = true
            flip.alpha = 0f
            flip.scale = 0.5f
            flip.animate().cancel()
            flip.animate().alpha(1f).scale(1f).setDuration(200).start()
        }

        if (state.swapFrom != null && swapFeature.swap.value?.from == null) {
            swapFeature.setFrom(state.swapFrom)
        }
        if (state.swapTo != null && swapFeature.swap.value?.to == null && swapFeature.swap.value?.initialTo != null) {
            swapFeature.setTo(state.swapTo)
        }
        valueRecView.isEnabled = state.swapTo != null
        valueView.isEnabled = state.swapFrom != null
        valueRecView.alpha = if (state.swapTo != null) 1f else 0.5f
        valueView.alpha = if (state.swapFrom != null) 1f else 0.5f

        valueView.setDecimalCount(state.decimals)
        valueRecView.setDecimalCount(state.decimalsRec)

        viewSwitcher(loader2, expand, state.loadingSimulation)
        viewSwitcher(loader, hintText, state.loadingSimulation)
        hintButton.isEnabled = !state.loadingSimulation

        if (state.simulateData != null && state.swapFrom != null && state.swapTo != null) {
            rate.visibilityHeightAnimated(true, 49.dp.toFloat())
            simulationView.setData(state.simulateData, state.swapFrom, state.swapTo, state.slippage)
            rateText.text = state.swapRate
            hintText.setText(Localization.continue_action)
            hintButton.isSelected = true
        } else {
            if (state.loadingSimulation) {
            } else {
                if (state.swapFrom == null) {
                    hintText.setText(Localization.choose_token)
                } else {
                    if (state.amount == 0f) {
                        hintText.setText(Localization.enter_amount)
                    } else {
                        hintText.setText(Localization.choose_token)
                    }
                }
            }
            rateText.text = ""
            rate.visibilityHeightAnimated(false, 49.dp.toFloat())
            secondDivider.visibility = View.GONE
            simulationView.visibility = View.GONE
            expand.rotation = 0f
            hintButton.isSelected = false
        }

        setFrom(state.swapFrom)
        setTo(state.swapTo)

        if (!pendingChangeValue) {
            valueView.setText("")
            valueView.append(state.swapFromAmount)
        }

        valueRecView.setText("")
        valueRecView.append(state.swapToAmount)

        if (state.insufficientBalance) {
            availableView.setText(Localization.insufficient_balance)
            availableView.setTextColor(requireContext().constantRedColor)
        } else {
            availableView.text = getString(Localization.available_balance2, state.available)
            availableView.setTextColor(requireContext().textSecondaryColor)
        }

        availableViewRec.text = getString(Localization.available_balance2, state.availableRec)
        availableViewRec.setTextColor(requireContext().textSecondaryColor)

        maxButton.visibility = if (state.swapFrom == null) View.INVISIBLE else View.VISIBLE
        availableView.visibility = if (state.swapFrom == null) View.INVISIBLE else View.VISIBLE
        availableViewRec.visibility = if (state.swapTo == null) View.INVISIBLE else View.VISIBLE

        maxButton.isActivated = state.maxActive
        changingState = false
    }

    override fun onVisibleChange(visible: Boolean) {
        super.onVisibleChange(visible)
        if (visible) {
            swapFeature.swap.value?.let {
                if (feature.uiState.value.wallet == null) {
                    feature.update(it)
                }
            }
            valueView.focusWithKeyboard()
        } else {
            valueView.hideKeyboard()
        }
    }
}