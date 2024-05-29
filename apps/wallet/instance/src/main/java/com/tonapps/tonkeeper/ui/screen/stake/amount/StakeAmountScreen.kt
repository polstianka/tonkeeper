package com.tonapps.tonkeeper.ui.screen.stake.amount


import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.ui.screen.stake.pager.PagerScreen
import com.tonapps.tonkeeper.ui.screen.stake.pager.StakeScreenAdapter
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.buttonPrimaryBackgroundColor
import com.tonapps.uikit.color.buttonSecondaryBackgroundColor
import com.tonapps.uikit.color.constantRedColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.extensions.focusWithKeyboard
import uikit.extensions.hideKeyboard
import uikit.extensions.setEndDrawable
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.Locale

class StakeAmountScreen : PagerScreen<StakeAmountScreenState, StakeAmountScreenEffect, StakeAmountScreenFeature>(R.layout.fragment_stake_amount) {

    companion object {
        fun newInstance() = StakeAmountScreen()
    }

    override val feature: StakeAmountScreenFeature by viewModel()

    private val sdf: SimpleDateFormat by lazy { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    private val handler = Handler()
    private lateinit var valueView: AmountInput
    private lateinit var valueCurrencyView: AppCompatTextView
    private lateinit var rateView: AppCompatTextView
    private lateinit var availableView: AppCompatTextView
    private lateinit var maxButton: Button
    private lateinit var stakeView: StakeView
    private lateinit var stakeContainer: View
    private lateinit var skeleton: View
    private lateinit var continueButton: Button
    private lateinit var unstakeHint: View
    private lateinit var unstakeHintText: AppCompatTextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        unstakeHint = view.findViewById(R.id.unstake_hint)
        unstakeHintText = view.findViewById(R.id.unstake_hint_text)
        stakeView = view.findViewById(R.id.stake)
        stakeContainer = view.findViewById(R.id.stake_container)
        stakeContainer.setOnClickListener {
            stakeFeature.setCurrentPage(StakeScreenAdapter.POSITION_OPTIONS)
        }

        skeleton = view.findViewById(R.id.skeleton)

        valueView = view.findViewById(R.id.value)
        valueView.doOnTextChanged { _, _, _, _ ->
            feature.setValue(getValue())
        }

        valueCurrencyView = view.findViewById(R.id.value_currency)

        rateView = view.findViewById(R.id.rate)
        availableView = view.findViewById(R.id.available)

        maxButton = view.findViewById(R.id.max)
        maxButton.setOnClickListener {
            if (maxButton.isActivated) {
                clearValue()
            } else {
                setMaxValue()
            }
        }

        continueButton = view.findViewById(R.id.continue_action)
        continueButton.setOnClickListener { next() }

        stakeFeature.data.observe(viewLifecycleOwner) { data ->
            feature.setPool(data.poolInfo)
        }

        if (stakeFeature.data.value?.preUnstake == true) {
            rateView.setEndDrawable(null)
        } else {
            rateView.setOnClickListener {
                // todo flip
            }
        }

        updateStakeHintText = Runnable {
            if (unstakeHint.isVisible) {
                feature.uiState.value.selectedPool?.let {
                    try {
                        val d: Duration = Duration.between(Instant.now(), Date(it.cycleEnd).toInstant())
                        val date = Date(d.toMillis().coerceAtMost(0L))
                        unstakeHintText.text = getString(Localization.unstake_hint, sdf.format(date))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    updateStakeHintText?.let { it1 -> handler.postDelayed(it1, 1000) }
                }
            }
        }
    }

    fun forceSetAmount(amount: Float) {
        val text = if (0f >= amount) {
            ""
        } else {
            amount.toString()
        }
        val editable = valueView.text ?: return
        editable.replace(0, editable.length, text)
    }

    private fun getValue(): Float {
        return valueView.text.toString().toFloatOrNull() ?: 0f
    }

    private fun next() {
        stakeFeature.setAmount(valueView.text.toString())
        stakeFeature.setCurrentPage(StakeScreenAdapter.POSITION_CONFIRM)
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

    override fun newUiState(state: StakeAmountScreenState) {
        skeleton.isVisible = state.loading && !state.isUnstake
        stakeContainer.isVisible = !state.loading && !state.isUnstake
        rateView.text = state.rate
        valueView.setDecimalCount(state.decimals)
        unstakeHint.isVisible = state.isUnstake

        valueCurrencyView.text = state.selectedTokenCode

        if (state.insufficientBalance) {
            availableView.setText(Localization.insufficient_balance)
            availableView.setTextColor(requireContext().constantRedColor)
        } else if (state.remaining != "") {
            availableView.text = getString(Localization.remaining_balance, state.remaining)
            availableView.setTextColor(requireContext().textSecondaryColor)
        } else {
            availableView.text = getString(Localization.available_balance, state.available)
            availableView.setTextColor(requireContext().textSecondaryColor)
        }

        continueButton.isEnabled = state.canContinue && !state.loading

        if (state.maxActive) {
            maxButton.background.setTint(requireContext().buttonPrimaryBackgroundColor)
        } else {
            maxButton.background.setTint(requireContext().buttonSecondaryBackgroundColor)
        }

        maxButton.isActivated = state.maxActive
        //stakeFeature.setMax(state.maxActive)

        if (state.selectedPool != null) {
            if (stakeFeature.data.value?.poolInfo == null) {
                stakeFeature.setPool(state.selectedPool)
            }
            if (state.isUnstake ) {
                updateStakeHintText?.let { handler.post(it) }
            }
            stakeView.setPool(state.selectedPool)
        }
    }

    private var updateStakeHintText: Runnable? = null

    override fun onStop() {
        super.onStop()
        updateStakeHintText?.let { handler.removeCallbacks(it) }
    }

    override fun onVisibleChange(visible: Boolean) {
        super.onVisibleChange(visible)
        if (visible) {
            if (stakeFeature.data.value?.preUnstake == true) {
                stakeFeature.setHeaderTitle(getString(Localization.unstake))
            } else {
                stakeFeature.setHeaderTitle(getString(Localization.stake))
            }
            stakeFeature.data.value?.let {
                feature.setData(it)
            }
            stakeFeature.setHeaderVisible(true)
            valueView.focusWithKeyboard()
            updateStakeHintText?.let {
                handler.removeCallbacks(it)
                handler.postDelayed(it, 1000)
            }
        } else {
            valueView.hideKeyboard()
        }
    }
}