package com.tonapps.tonkeeper.ui.screen.stake.amount

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.helper.NumberFormatter
import com.tonapps.tonkeeper.ui.screen.stake.StakeMainViewModel
import com.tonapps.tonkeeper.ui.screen.stake.model.icon
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.buttonPrimaryBackgroundColor
import com.tonapps.uikit.color.buttonSecondaryBackgroundColor
import com.tonapps.uikit.color.constantRedColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.extensions.collectFlow
import uikit.widget.ActionCellView
import uikit.widget.LoaderView

class StakeAmountScreen : Fragment(R.layout.fragment_stake) {
    private val stakeViewModel: StakeViewModel by viewModel()
    private val stakeMainViewModel: StakeMainViewModel by activityViewModel()

    private lateinit var selectedPool: ActionCellView
    private lateinit var valueCurrencyView: AppCompatTextView
    private lateinit var rateView: AppCompatTextView
    private lateinit var availableView: AppCompatTextView
    private lateinit var maxButton: Button
    private lateinit var continueButton: Button
    private lateinit var valueView: AmountInput
    private lateinit var loaderView: LoaderView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stakeViewModel.setAddress(stakeMainViewModel.preselectedAddress)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        selectedPool = view.findViewById(R.id.selected_pool)
        valueCurrencyView = view.findViewById(R.id.value_currency)
        loaderView = view.findViewById(R.id.loading_view)

        valueView = view.findViewById(R.id.value)
        valueView.doOnTextChanged { _, _, _, _ ->
            stakeViewModel.setValue(getValue())
        }

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
        continueButton.setOnClickListener { stakeViewModel.onContinue() }

        val savedButtonText = continueButton.text

        collectFlow(stakeViewModel.uiState) { state ->
            rateView.text = state.rate
            valueView.setMaxLength(stakeViewModel.decimals)
            valueCurrencyView.text = state.selectedTokenCode

            if (state.loading) {
                continueButton.text = ""
                loaderView.isVisible = true
            } else {
                continueButton.text = savedButtonText
                loaderView.isVisible = false
            }

            if (state.minWarning.isNotEmpty()) {
                availableView.text =
                    getString(Localization.minimum_amout_placeholder, state.minWarning)
                availableView.setTextColor(requireContext().constantRedColor)
            } else if (state.insufficientBalance) {
                availableView.setText(Localization.insufficient_balance)
                availableView.setTextColor(requireContext().constantRedColor)
            } else if (state.remaining != "") {
                availableView.text = getString(Localization.remaining_balance, state.remaining)
                availableView.setTextColor(requireContext().textSecondaryColor)
            } else {
                availableView.text = getString(Localization.available_balance, state.available)
                availableView.setTextColor(requireContext().textSecondaryColor)
            }

            continueButton.isEnabled = state.canContinue

            if (state.maxActive) {
                maxButton.background.setTint(requireContext().buttonPrimaryBackgroundColor)
            } else {
                maxButton.background.setTint(requireContext().buttonSecondaryBackgroundColor)
            }

            maxButton.isActivated = state.maxActive
            selectedPool.isVisible = state.selectedPool != null
            if (state.selectedPool != null) {
                with(selectedPool) {
                    title = state.selectedPool.pool.name
                    titleBadgeText = getString(Localization.max_apy).takeIf {
                        state.selectedPool.isMaxApy
                    }
                    iconRes = state.selectedPool.pool.implementation.icon
                    iconTint = 0
                    isRoundedIcon = true
                    actionTint = com.tonapps.uikit.color.R.attr.iconTertiaryColor
                    subtitle = getString(
                        Localization.apy_percent_placeholder,
                        NumberFormatter.format(state.selectedPool.pool.apy)
                    )
                    setOnClickListener { stakeMainViewModel.openOptions() }
                }
            }

            if (state.confirmScreenArgs != null) {
                stakeMainViewModel.onConfirmationArgsReceived(state.confirmScreenArgs)
            }
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

    private fun setMaxValue() {
        val maxValue = stakeViewModel.currentBalance
        val text = valueView.text ?: return
        val format = CurrencyFormatter.format(value = maxValue, decimals = stakeViewModel.decimals)
        text.replace(0, text.length, format)
    }

    private fun clearValue() {
        forceSetAmount(0f)
    }

    private fun getValue(): Float {
        val text = Coin.prepareValue(valueView.text.toString())
        return text.toFloatOrNull() ?: 0f
    }

    companion object {
        fun newInstance() = StakeAmountScreen()
    }
}