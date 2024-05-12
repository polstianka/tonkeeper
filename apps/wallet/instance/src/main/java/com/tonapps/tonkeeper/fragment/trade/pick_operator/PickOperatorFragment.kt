package com.tonapps.tonkeeper.fragment.trade.pick_operator

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.tonapps.tonkeeper.fragment.trade.pick_currency.PickCurrencyFragment
import com.tonapps.tonkeeper.fragment.trade.pick_currency.PickCurrencyResult
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.setThrottleClickListener
import uikit.navigation.Navigation
import uikit.widget.DropdownButton
import uikit.widget.HeaderView
import com.tonapps.uikit.icon.UIKitIcon

class PickOperatorFragment : BaseFragment(R.layout.fragment_pick_operator),
    BaseFragment.BottomSheet {
    companion object {
        fun newInstance(
            id: String,
            name: String,
            country: String,
            selectedCurrencyCode: String,
            amount: Float
        ): PickOperatorFragment {
            val argument = PickOperatorFragmentArgs(
                id = id,
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
    private val navigation
        get() = context?.let { Navigation.from(it) }

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
        header?.setAction(UIKitIcon.ic_close_16)
        header?.doOnActionClick = { viewModel.onCrossClicked() }
        header?.setIcon(UIKitIcon.ic_chevron_left_16)
        header?.doOnCloseClick = { viewModel.onChevronClicked() }
        currencyDropdown?.setThrottleClickListener { viewModel.onCurrencyDropdownClicked() }
        observeFlows()
    }

    private fun observeFlows() {
        observeFlow(viewModel.subtitleText) { header?.setSubtitle(it) }
        observeFlow(viewModel.events) { handleEvent(it) }
        observeFlow(viewModel.currencyCode) { currencyTitle?.text = it }
        observeFlow(viewModel.currencyName) { currencyDescription?.setText(it) }
    }

    private fun handleEvent(it: PickOperatorEvents) {
        when (it) {
            PickOperatorEvents.CloseFlow -> Log.wtf("###", "close flow")
            PickOperatorEvents.NavigateBack -> finish()
            is PickOperatorEvents.PickCurrency -> it.handle()
        }
    }

    private fun onPickCurrencyResult(bundle: Bundle) {
        val result = PickCurrencyResult(bundle)
        viewModel.onCurrencyPicked(result)
    }

    private fun PickOperatorEvents.PickCurrency.handle() {
        navigation?.add(PickCurrencyFragment.newInstance(paymentMethodId, pickedCurrencyCode))
    }
}