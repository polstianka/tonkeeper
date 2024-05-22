package com.tonapps.tonkeeper.fragment.trade.exchange

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.extensions.doOnAmountChange
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.fragment.trade.domain.model.ExchangeDirection
import com.tonapps.tonkeeper.fragment.trade.exchange.vm.ExchangeEvent
import com.tonapps.tonkeeper.fragment.trade.exchange.vm.ExchangeViewModel
import com.tonapps.tonkeeper.fragment.trade.pick_operator.PickOperatorFragment
import com.tonapps.tonkeeper.fragment.trade.ui.rv.TradeAdapter
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.cornerMedium
import uikit.extensions.dp
import uikit.extensions.round
import uikit.extensions.setThrottleClickListener
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.SimpleRecyclerView
import java.math.BigDecimal
import com.tonapps.wallet.localization.R as LocalizationR

class ExchangeFragment : BaseFragment(R.layout.fragment_exchange) {
    companion object {
        fun newInstance(direction: ExchangeDirection) = ExchangeFragment().apply {
            setArgs(ExchangeFragmentArgs(direction))
        }
    }

    private val viewModel: ExchangeViewModel by viewModel()
    private val input: AmountInput?
        get() = view?.findViewById(R.id.fragment_exchange_input)
    private val rateTextView: AppCompatTextView?
        get() = view?.findViewById(R.id.fragment_exchange_rate)
    private val adapter = TradeAdapter { viewModel.onTradeMethodClicked(it) }
    private val recyclerView: SimpleRecyclerView?
        get() = view?.findViewById(R.id.fragment_exchange_rv)
    private val button: Button?
        get() = view?.findViewById(R.id.fragment_exchange_button)
    private val minAmountLabel: TextView?
        get() = view?.findViewById(R.id.fragment_exchange_min_amount)
    private val footer: View?
        get() = view?.findViewById(R.id.fragment_exchange_footer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.provideArgs(ExchangeFragmentArgs(requireArguments()))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        input?.doOnAmountChange { viewModel.onAmountChanged(it) }
        recyclerView?.adapter = adapter
        observeFlow(viewModel.totalFiat) { rateTextView?.text = it }
        observeFlow(viewModel.methods) { adapter.submitList(it) }
        observeFlow(viewModel.isButtonActive) { button?.isEnabled = it }
        observeFlow(viewModel.events) { handleEvent(it) }
        observeFlow(viewModel.minAmount) { updateMinAmount(it) }
        // clip children ripple effect
        recyclerView?.round(requireContext().cornerMedium)
        button?.setThrottleClickListener { viewModel.onButtonClicked() }
        footer?.applyNavBottomPadding(16f.dp.toInt())
    }

    private fun updateMinAmount(minAmount: BigDecimal) {
        val amount = CurrencyFormatter.format(
            "TON",
            minAmount
        )
        minAmountLabel?.text = getString(LocalizationR.string.min_amount_mask, amount)
    }

    private fun handleEvent(event: ExchangeEvent) {
        when (event) {
            is ExchangeEvent.NavigateToPickOperator -> event.handle()
        }
    }

    private fun ExchangeEvent.NavigateToPickOperator.handle() {

        val fragment = PickOperatorFragment.newInstance(
            id = paymentMethodId,
            name = paymentMethodName,
            country = country,
            selectedCurrencyCode = currencyCode,
            amount = amount,
            direction = direction
        )
        navigation?.add(fragment)
    }
}