package com.tonapps.tonkeeper.fragment.stake.unstake

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.toString
import com.tonapps.tonkeeper.fragment.stake.confirm.ConfirmStakeFragment
import com.tonapps.tonkeeper.fragment.stake.domain.StakingTransactionType
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakedBalance
import com.tonapps.tonkeeper.fragment.stake.ui.StakeInputView
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.setThrottleClickListener
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView
import java.math.BigDecimal

class UnstakeFragment : BaseFragment(R.layout.fragment_unstake), BaseFragment.BottomSheet {

    companion object {
        fun newInstance(balance: StakedBalance) = UnstakeFragment().apply {
            setArgs(
                UnstakeArgs(balance)
            )
        }
    }

    private val viewModel: UnstakeViewModel by viewModel()
    private val header: HeaderView?
        get() = view?.findViewById(R.id.fragment_unstake_header)
    private val button: Button?
        get() = view?.findViewById(R.id.fragment_unstake_button)
    private val footer: View?
        get() = view?.findViewById(R.id.fragment_unstake_footer)
    private val description: TextView?
        get() = view?.findViewById(R.id.fragment_unstake_label)
    private val input: StakeInputView?
        get() = view?.findViewById(R.id.fragment_unstake_input)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.provideArgs(
                UnstakeArgs(requireArguments())
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header?.doOnActionClick = { viewModel.onCloseClicked() }

        footer?.applyNavBottomPadding()

        input?.setOnMaxClickedListener { viewModel.onMaxClicked() }
        input?.setOnAmountChangedListener { viewModel.onAmountChanged(it) }

        button?.setThrottleClickListener { viewModel.onButtonClicked() }

        observeFlow(viewModel.events) { handleEvents(it) }
        observeFlow(viewModel.description) { description?.text = toString(it) }
        observeFlow(viewModel.available) { available ->
            updateLabelText(available)
        }
        observeFlow(viewModel.fiatText) { input?.setFiatText(it.toString()) }
        observeFlow(viewModel.isButtonEnabled) { button?.isEnabled = it }
    }

    private fun updateLabelText(available: BigDecimal) {
        val isPositive = available >= BigDecimal.ZERO
        val text = if (isPositive) {
            CurrencyFormatter.format("TON", available)
                .let { getString(com.tonapps.wallet.localization.R.string.available_balance, it) }
        } else {
            getString(com.tonapps.wallet.localization.R.string.insufficient_balance)
        }
        val textColorAttribute = if (isPositive) {
            com.tonapps.uikit.color.R.attr.textSecondaryColor
        } else {
            com.tonapps.uikit.color.R.attr.accentRedColor
        }
        input?.setLabelText(text)
        input?.setLabelTextColorAttribute(textColorAttribute)
    }

    private fun handleEvents(event: UnstakeEvent) {
        when (event) {
            UnstakeEvent.NavigateBack -> finish()
            is UnstakeEvent.FillInput -> event.handle()
            is UnstakeEvent.NavigateToConfirmStake -> event.handle()
        }
    }

    private fun UnstakeEvent.NavigateToConfirmStake.handle() {
        val fragment = ConfirmStakeFragment.newInstance(
            pool,
            amount,
            StakingTransactionType.UNSTAKE,
            isSendAll
        )
        navigation?.add(fragment)
    }

    private fun UnstakeEvent.FillInput.handle() {
        input?.setInputText(text)
    }
}