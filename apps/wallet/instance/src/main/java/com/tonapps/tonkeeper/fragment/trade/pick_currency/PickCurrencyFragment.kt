package com.tonapps.tonkeeper.fragment.trade.pick_currency

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeper.fragment.trade.domain.model.ExchangeDirection
import com.tonapps.tonkeeper.ui.screen.settings.currency.list.CurrencyAdapter
import core.extensions.observeFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.base.BaseListFragment
import uikit.navigation.Navigation.Companion.navigation

class PickCurrencyFragment : BaseListFragment(), BaseFragment.BottomSheet {

    companion object {
        fun newInstance(
            paymentMethodId: String,
            pickedCurrencyCode: String,
            direction: ExchangeDirection
        ): PickCurrencyFragment {
            val args = PickCurrencyFragmentArgs(
                paymentMethodId = paymentMethodId,
                pickedCurrencyCode = pickedCurrencyCode,
                direction = direction
            )
            return PickCurrencyFragment().apply { setArgs(args) }
        }
    }

    private val viewModel: PickCurrencyViewModel by viewModel()
    private val adapter = CurrencyAdapter { viewModel.onCurrencyClicked(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.provideArgs(PickCurrencyFragmentArgs(requireArguments()))
        }
        lifecycleScope.launch {
            viewModel.events.collectLatest { handleEvent(it) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle(getString(com.tonapps.wallet.localization.R.string.currency))
        setAdapter(adapter)

        observeFlow(viewModel.items) { adapter.submitList(it) }
    }

    private fun handleEvent(event: PickCurrencyEvent) {
        when (event) {
            is PickCurrencyEvent.ReturnWithResult -> {
                navigation?.setFragmentResult(
                    PickCurrencyResult.KEY_REQUEST,
                    PickCurrencyResult(event.currencyCode).toBundle()
                )
                finish()
            }
        }
    }
}