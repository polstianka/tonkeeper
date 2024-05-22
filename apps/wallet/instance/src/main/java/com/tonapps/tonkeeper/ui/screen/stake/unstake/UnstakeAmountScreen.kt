package com.tonapps.tonkeeper.ui.screen.stake.unstake

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.ui.screen.stake.StakeMainViewModel
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.buttonPrimaryBackgroundColor
import com.tonapps.uikit.color.buttonSecondaryBackgroundColor
import com.tonapps.uikit.color.constantRedColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.extensions.collectFlow

class UnstakeAmountScreen : Fragment(R.layout.fragment_unstake) {
    private val stakeViewModel: UnstakeViewModel by viewModel()
    private val stakeMainViewModel: StakeMainViewModel by activityViewModel()

    private lateinit var valueCurrencyView: AppCompatTextView
    private lateinit var rateView: AppCompatTextView
    private lateinit var availableView: AppCompatTextView
    private lateinit var maxButton: Button
    private lateinit var continueButton: Button
    private lateinit var valueView: AmountInput
    private lateinit var disclaimerView: AppCompatTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stakeViewModel.load(stakeMainViewModel.preselectedAddress)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        valueCurrencyView = view.findViewById(R.id.value_currency)
        disclaimerView = view.findViewById(R.id.unstake_disclaimer)

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

        collectFlow(stakeViewModel.uiState) { state ->
            rateView.text = state.rate
            valueView.setMaxLength(stakeViewModel.decimals)
            valueCurrencyView.text = state.selectedTokenCode
            disclaimerView.text =
                getString(Localization.unstake_disclaimer_placeholder, state.timerValue)

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

            continueButton.isEnabled = state.canContinue

            if (state.maxActive) {
                maxButton.background.setTint(requireContext().buttonPrimaryBackgroundColor)
            } else {
                maxButton.background.setTint(requireContext().buttonSecondaryBackgroundColor)
            }

            maxButton.isActivated = state.maxActive
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
        fun newInstance() = UnstakeAmountScreen()
    }
}