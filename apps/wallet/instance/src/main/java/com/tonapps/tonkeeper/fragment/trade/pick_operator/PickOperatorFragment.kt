package com.tonapps.tonkeeper.fragment.trade.pick_operator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.extensions.popBackToRootFragment
import com.tonapps.tonkeeper.fragment.fiat.web.FiatWebFragment
import com.tonapps.tonkeeper.fragment.trade.domain.model.ExchangeDirection
import com.tonapps.tonkeeper.fragment.trade.pick_currency.PickCurrencyFragment
import com.tonapps.tonkeeper.fragment.trade.pick_currency.PickCurrencyResult
import com.tonapps.tonkeeper.fragment.trade.pick_operator.rv.PaymentOperatorAdapter
import com.tonapps.tonkeeper.fragment.trade.root.presentation.BuySellFragment
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.icon.UIKitIcon
import core.extensions.observeFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.dp
import uikit.extensions.setThrottleClickListener
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.DropdownButton
import uikit.widget.HeaderView
import java.math.BigDecimal

class PickOperatorFragment : BaseFragment(R.layout.fragment_pick_operator),
    BaseFragment.BottomSheet {
    companion object {
        fun newInstance(
            direction: ExchangeDirection,
            id: String,
            name: String,
            country: String,
            selectedCurrencyCode: String,
            amount: BigDecimal
        ): PickOperatorFragment {
            val argument = PickOperatorFragmentArgs(
                exchangeDirection = direction,
                paymentMethodId = id,
                name = name,
                country = country,
                selectedCurrencyCode = selectedCurrencyCode,
                amount = amount
            )
            return PickOperatorFragment().apply { setArgs(argument) }
        }
    }

    private val header: HeaderView?
        get() = view?.findViewById(R.id.fragment_pick_operator_header)
    private val currencyTitle: TextView?
        get() = view?.findViewById(R.id.fragment_pick_operator_currency_title)
    private val currencyDescription: TextView?
        get() = view?.findViewById(R.id.fragment_pick_operator_currency_description)
    private val viewModel: PickOperatorViewModel by viewModel()
    private val currencyDropdown: DropdownButton?
        get() = view?.findViewById(R.id.fragment_pick_operator_currency_dropdown)
    private val recyclerView: RecyclerView?
        get() = view?.findViewById(R.id.fragment_pick_operator_recycler_view)
    private val button: Button?
        get() = view?.findViewById(R.id.fragment_pick_operator_button)
    private val buttonContainer: View?
        get() = view?.findViewById(R.id.fragment_pick_operator_button_container)
    private val adapter = PaymentOperatorAdapter { viewModel.onPaymentOperatorClicked(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.provideArguments(
                PickOperatorFragmentArgs(requireArguments())
            )
        }
        navigation?.setFragmentResultListener(
            PickCurrencyResult.KEY_REQUEST,
            ::onPickCurrencyResult
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonContainer?.applyNavBottomPadding(32f.dp)
        header?.setAction(UIKitIcon.ic_close_16)
        header?.doOnActionClick = { viewModel.onCrossClicked() }
        header?.setIcon(UIKitIcon.ic_chevron_left_16)
        header?.doOnCloseClick = { viewModel.onChevronClicked() }
        currencyDropdown?.setThrottleClickListener { viewModel.onCurrencyDropdownClicked() }
        button?.setOnClickListener { viewModel.onButtonClicked() }
        recyclerView?.adapter = adapter
        observeFlows()
    }

    private fun observeFlows() {
        observeFlow(viewModel.subtitleText) { header?.setSubtitle(it) }
        observeFlow(viewModel.events) { handleEvent(it) }
        observeFlow(viewModel.currencyCode) { currencyTitle?.text = it }
        observeFlow(viewModel.currencyName) { currencyDescription?.setText(it) }
        observeFlow(viewModel.paymentOperators) { list ->
            adapter.submitList(list)
        }
    }

    private fun handleEvent(it: PickOperatorEvents) {
        when (it) {
            PickOperatorEvents.CloseFlow -> dismissFlow()
            PickOperatorEvents.NavigateBack -> finish()
            is PickOperatorEvents.PickCurrency -> it.handle()
            is PickOperatorEvents.NavigateToWebView -> it.handle()
        }
    }

    private fun dismissFlow() {
        popBackToRootFragment(includingRoot = true, BuySellFragment::class)
        finish()
    }

    private fun PickOperatorEvents.NavigateToWebView.handle() {
        dismissFlow()
        val fragment = FiatWebFragment.newInstance(url, successUrlPattern)
        navigation?.add(fragment)
    }

    private fun onPickCurrencyResult(bundle: Bundle) {
        val result = PickCurrencyResult(bundle)
        viewModel.onCurrencyPicked(result)
    }

    private fun PickOperatorEvents.PickCurrency.handle() {
        navigation?.add(
            PickCurrencyFragment.newInstance(
                paymentMethodId = paymentMethodId,
                pickedCurrencyCode = pickedCurrencyCode,
                direction = direction
            )
        )
    }
}