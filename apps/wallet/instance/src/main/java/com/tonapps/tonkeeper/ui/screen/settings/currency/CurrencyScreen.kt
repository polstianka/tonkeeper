package com.tonapps.tonkeeper.ui.screen.settings.currency

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.tonapps.tonkeeper.ui.screen.settings.currency.list.Adapter
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.base.BaseListFragment
import uikit.extensions.collectFlow
import uikit.navigation.Navigation.Companion.navigation

class CurrencyScreen : BaseListFragment(), BaseFragment.SwipeBack {

    private val currencyViewModel: CurrencyViewModel by viewModel()

    private val adapter = Adapter(::selectCurrency)

    private val request: String by lazy { arguments?.getString(REQUEST_KEY).orEmpty() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle(getString(Localization.primary_currency))
        setAdapter(adapter)

        collectFlow(currencyViewModel.uiItemsFlow, adapter::submitList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        navigation?.setFragmentResult(request)
    }

    private fun selectCurrency(currency: String) {
        currencyViewModel.selectCurrency(currency)
        finish()
    }

    companion object {
        private const val REQUEST_KEY = "request"

        const val CURRENCY_DIALOG_REQUEST = "currency_dialog_request"

        fun newInstance() = CurrencyScreen()

        fun newInstance(tag: String) = CurrencyScreen().apply {
            arguments = bundleOf(REQUEST_KEY to tag)
        }
    }
}