package com.tonapps.tonkeeper.ui.screen.buysell.currency

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.ui.screen.buysell.currency.list.BuySellCurrencyAdapter
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.base.BaseListFragment
import uikit.extensions.collectFlow

class BuySellCurrencyScreen: BaseListFragment(), BaseFragment.BottomSheet {

    private val currencyViewModel: BuySellCurrencyViewModel by viewModel()

    private val adapter = BuySellCurrencyAdapter(::selectCurrency)

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
        fun newInstance() = BuySellCurrencyScreen()
    }
}