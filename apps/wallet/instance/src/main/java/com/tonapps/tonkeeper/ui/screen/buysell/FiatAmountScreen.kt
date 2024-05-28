package com.tonapps.tonkeeper.ui.screen.buysell

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.fragment.country.CountryScreen
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.collectFlow
import uikit.extensions.getDimensionPixelSize
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.SimpleRecyclerView

class FiatAmountScreen : BaseFragment(R.layout.fragment_fiat_amount), BaseFragment.BottomSheet {

    private val amountViewModel: FiatAmountViewModel by viewModel()

    private lateinit var headerView: FiatHeaderView
    private lateinit var valueCurrencyView: AppCompatTextView
    private lateinit var rateView: AppCompatTextView
    private lateinit var valueView: AmountInput
    private lateinit var methodsListView: SimpleRecyclerView
    private lateinit var continueButton: Button

    private val adapter = MethodTypeAdapter {
        amountViewModel.onMethodSelected(it)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        valueCurrencyView = view.findViewById(R.id.value_currency)
        rateView = view.findViewById(R.id.rate)

        headerView = view.findViewById(R.id.header)
        headerView.doOnCountryClick = {
            navigation?.setFragmentResultListener(FIAT_DIALOG_REQUEST) {
                amountViewModel.reloadCountry()
            }
            navigation?.add(CountryScreen.newInstance(FIAT_DIALOG_REQUEST))
        }
        headerView.doOnCloseClick = { finish() }
        headerView.doOnTypeChange = { amountViewModel.updateOperationType(it) }
        view.findViewById<View>(R.id.root)
            .applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium))

        valueView = view.findViewById(R.id.value)
        valueView.doOnTextChanged { _, _, _, _ ->
            amountViewModel.setValue(getValue())
        }

        continueButton = view.findViewById(R.id.continue_action)
        continueButton.setOnClickListener {
            val selected = amountViewModel.getSelectedType()
            val args = FiatOperatorArgs(
                type = selected.type,
                name = selected.name,
                operationType = amountViewModel.uiState.value.fiatOperation,
                amount = getValue()
            )
            navigation?.add(FiatOperatorScreen.newInstance(args))
        }

        methodsListView = view.findViewById(R.id.methods_list)
        methodsListView.adapter = adapter

        collectFlow(amountViewModel.uiState) { state ->
            headerView.countryCode = state.countryCode
            rateView.text = state.rate
            valueCurrencyView.text = "TON"
            continueButton.isEnabled = state.canContinue

            adapter.submitList(state.methodTypes)
        }
    }

    private fun getValue(): Float {
        val text = Coin.prepareValue(valueView.text.toString())
        return text.toFloatOrNull() ?: 0f
    }

    companion object {
        const val FIAT_DIALOG_REQUEST = "fiat_dialog_request"

        fun newInstance() = FiatAmountScreen()
    }
}