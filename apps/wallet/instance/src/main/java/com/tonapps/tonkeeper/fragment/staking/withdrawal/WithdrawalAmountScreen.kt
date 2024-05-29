package com.tonapps.tonkeeper.fragment.staking.withdrawal

import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.tonapps.tonkeeper.extensions.findParent
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.fragment.staking.deposit.view.PoolSelectedView
import com.tonapps.tonkeeper.helper.flow.CountdownTimer
import com.tonapps.tonkeeper.ui.component.BlurredScrollView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundTransparentColor
import com.tonapps.uikit.color.buttonPrimaryBackgroundColor
import com.tonapps.uikit.color.buttonSecondaryBackgroundColor
import com.tonapps.uikit.color.constantRedColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.bottomScrolled
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.extensions.hideKeyboard
import uikit.widget.FooterViewEmpty

class WithdrawalAmountScreen(
) : BaseFragment(R.layout.fragment_stake_amount) {
    private val withdrawalScreenViewModel: WithdrawalScreenViewModel by viewModel(ownerProducer = { this.findParent<WithdrawalScreen>() })

    private lateinit var valueView: AmountInput
    private lateinit var rateView: AppCompatTextView
    private lateinit var availableView: AppCompatTextView
    private lateinit var hintView: AppCompatTextView
    private lateinit var maxButton: Button
    private lateinit var continueButton: Button
    private lateinit var selectedPoolView: PoolSelectedView

    private lateinit var listView: BlurredScrollView
    private lateinit var footerView: FooterViewEmpty

    private var ignoreTextChanged: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = view.findViewById(R.id.list)
        listView.blurDisabled = true
        listView.blurredPaddingTop = 64.dp
        listView.blurredPaddingBottom = 180.dp

        footerView = view.findViewById(R.id.footer)
        footerView.setColor(requireContext().backgroundTransparentColor)

        collectFlow(listView.bottomScrolled, footerView::setDivider)

        availableView = view.findViewById(R.id.available)
        rateView = view.findViewById(R.id.rate)

        hintView = view.findViewById(R.id.unstake_hint)
        hintView.isVisible = true

        maxButton = view.findViewById(R.id.max)
        maxButton.setOnClickListener {
            withdrawalScreenViewModel.inputAmountController.toggleMax()
        }

        valueView = view.findViewById(R.id.value)
        valueView.doOnTextChanged { text, _, _, _ ->
            withdrawalScreenViewModel.inputAmountController.onInput(
                text
            )
        }

        continueButton = view.findViewById(R.id.continue_action)
        continueButton.setOnClickListener {
            getCurrentFocus()?.hideKeyboard()
            withdrawalScreenViewModel.openConfirmPage()
        }

        selectedPoolView = view.findViewById(R.id.selected)
        selectedPoolView.isVisible = false

        collectFlow(withdrawalScreenViewModel.countdownFlow) { seconds ->
            hintView.text =
                getString(Localization.staking_unstake_hint, CountdownTimer.format(seconds))
        }

        collectFlow(withdrawalScreenViewModel.amountScreenStateFlow) { state ->
            ignoreTextChanged = true
            if (valueView.text.toString() != state.input.input.input) {
                valueView.text = Editable.Factory.getInstance().newEditable(state.input.input.input)
            }
            ignoreTextChanged = false

            rateView.text = state.fiatFmt

            maxButton.isActivated = state.input.useMaxAmount
            if (state.input.useMaxAmount) {
                maxButton.background.setTint(requireContext().buttonPrimaryBackgroundColor)
            } else {
                maxButton.background.setTint(requireContext().buttonSecondaryBackgroundColor)
            }

            if (state.input.isTooBig) {
                availableView.setText(Localization.insufficient_balance)
                availableView.setTextColor(requireContext().constantRedColor)
            } else if (state.input.isValid) {
                availableView.text =
                    getString(Localization.remaining_balance, state.input.remainingFmt)
                availableView.setTextColor(requireContext().textSecondaryColor)
            } else {
                availableView.text =
                    getString(Localization.available_balance, state.input.availableFmt)
                availableView.setTextColor(requireContext().textSecondaryColor)
            }

            continueButton.isEnabled = state.input.isCorrectAndNonZero
        }
    }

    companion object {
        fun newInstance() = WithdrawalAmountScreen()
    }
}