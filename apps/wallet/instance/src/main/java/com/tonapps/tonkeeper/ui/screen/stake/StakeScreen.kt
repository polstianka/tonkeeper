package com.tonapps.tonkeeper.ui.screen.stake

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.buttonPrimaryBackgroundColor
import com.tonapps.uikit.color.buttonSecondaryBackgroundColor
import com.tonapps.uikit.color.constantRedColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.ActionCellView
import uikit.widget.HeaderView

class StakeScreen : BaseFragment(R.layout.fragment_stake), BaseFragment.BottomSheet {
    private val stakeViewModel: StakeViewModel by viewModel()

    private lateinit var headerView: HeaderView
    private lateinit var verticalLayout: ViewGroup
    private lateinit var valueCurrencyView: AppCompatTextView
    private lateinit var rateView: AppCompatTextView
    private lateinit var availableView: AppCompatTextView
    private lateinit var maxButton: Button
    private lateinit var continueButton: Button
    private lateinit var valueView: AmountInput

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }

        verticalLayout = view.findViewById(R.id.vertical_layout)
        valueCurrencyView = view.findViewById(R.id.value_currency)

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
        continueButton.setOnClickListener { }

        collectFlow(stakeViewModel.uiState) {
            rateView.text = it.rate
            valueView.setMaxLength(stakeViewModel.decimals)
            valueCurrencyView.text = it.selectedTokenCode

            if (it.insufficientBalance) {
                availableView.setText(Localization.insufficient_balance)
                availableView.setTextColor(requireContext().constantRedColor)
            } else if (it.remaining != "") {
                availableView.text = getString(Localization.remaining_balance, it.remaining)
                availableView.setTextColor(requireContext().textSecondaryColor)
            } else {
                availableView.text = getString(Localization.available_balance, it.available)
                availableView.setTextColor(requireContext().textSecondaryColor)
            }

            continueButton.isEnabled = it.canContinue

            if (it.maxActive) {
                maxButton.background.setTint(requireContext().buttonPrimaryBackgroundColor)
            } else {
                maxButton.background.setTint(requireContext().buttonSecondaryBackgroundColor)
            }

            maxButton.isActivated = it.maxActive

            if (it.maxPool != null) {
                verticalLayout.findViewWithTag<View>(it.maxPool.pool.name).apply {
                    verticalLayout.removeView(this)
                }
                verticalLayout.addView(ActionCellView(requireContext()).apply {
                    tag = it.maxPool.pool.name
                    actionRes = com.tonapps.uikit.icon.R.drawable.ic_switch_16
                    title = it.maxPool.pool.name
                    titleBadgeText = "max apy"
                    actionTint = com.tonapps.uikit.color.R.attr.iconTertiaryColor
                    subtitle = "APY ≈ ${it.maxPool.pool.apy.toPlainString()}%"
                    setOnClickListener { navigation?.add(StakeOptionsScreen()) }
                })
            }
        }


        stakeViewModel.init()
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
}