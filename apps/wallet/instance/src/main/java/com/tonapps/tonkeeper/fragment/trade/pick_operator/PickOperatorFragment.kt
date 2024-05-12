package com.tonapps.tonkeeper.fragment.trade.pick_operator

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.tonapps.tonkeeper.fragment.trade.pick_currency.PickCurrencyFragment
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.setThrottleClickListener
import uikit.navigation.Navigation
import uikit.widget.DropdownButton

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

    private val chevron: View?
        get() = view?.findViewById(R.id.fragment_pick_operator_header_chevron)
    private val cross: View?
        get() = view?.findViewById(R.id.fragment_pick_operator_header_cross)
    private val subtitle: TextView?
        get() = view?.findViewById(R.id.fragment_pick_operator_header_subtitle)
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chevron?.setThrottleClickListener { viewModel.onChevronClicked() }
        cross?.setThrottleClickListener { viewModel.onCrossClicked() }
        currencyDropdown?.setThrottleClickListener { viewModel.onCurrencyDropdownClicked() }
        observeFlows()
    }

    private fun observeFlows() {
        observeFlow(viewModel.subtitleText) { subtitle?.text = it }
        observeFlow(viewModel.events) { handleEvent(it) }
        observeFlow(viewModel.currencyCode) { currencyTitle?.text = it }
        observeFlow(viewModel.currencyName) { currencyDescription?.text = it }
    }

    private fun handleEvent(it: PickOperatorEvents) {
        when (it) {
            PickOperatorEvents.CloseFlow -> Log.wtf("###", "close flow")
            PickOperatorEvents.NavigateBack -> finish()
            is PickOperatorEvents.PickCurrency -> it.handle()
        }
    }

    private fun PickOperatorEvents.PickCurrency.handle() {
        navigation?.add(PickCurrencyFragment.newInstance(paymentMethodId, pickedCurrencyCode))
    }
}