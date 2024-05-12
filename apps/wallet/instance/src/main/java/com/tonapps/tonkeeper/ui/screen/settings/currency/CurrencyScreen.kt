package com.tonapps.tonkeeper.ui.screen.settings.currency

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.ui.screen.settings.currency.list.CurrencyAdapter
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.base.BaseListFragment
import uikit.extensions.collectFlow

class CurrencyScreen: BaseListFragment(), BaseFragment.SwipeBack {

    private val currencyViewModel: CurrencyViewModel by viewModel()

    private val adapter = CurrencyAdapter(::selectCurrency)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle(getString(Localization.primary_currency))
        setAdapter(adapter)

        collectFlow(currencyViewModel.uiItemsFlow, adapter::submitList)
    }

    private fun selectCurrency(currency: String) {
        currencyViewModel.selectCurrency(currency)
        finish()
    }

    companion object {
        fun newInstance() = CurrencyScreen()
    }
}