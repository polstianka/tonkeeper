package com.tonapps.tonkeeper.ui.screen.buysell.currency

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.ui.screen.buysell.pager.PagerScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.widget.SimpleRecyclerView
import com.tonapps.tonkeeper.ui.screen.settings.currency.list.Adapter
import com.tonapps.wallet.data.core.WalletCurrency
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.getDimensionPixelSize

class BuySellCurrencyScreen :
    PagerScreen<BuySellCurrencyScreenState, BuySellCurrencyScreenEffect, BuySellCurrencyScreenFeature>(
        R.layout.fragment_buysell_currency
    ) {

    companion object {
        fun newInstance() = BuySellCurrencyScreen()
    }

    override val feature: BuySellCurrencyScreenFeature by viewModel()

    private val adapter: Adapter by lazy {
        Adapter {
            buySellFeature.setCurrency(WalletCurrency(it))
            feature.setCurrency(it)
        }
    }

    private lateinit var list: SimpleRecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list = view.findViewById(R.id.list)
        list.adapter = adapter
        list.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.cornerMedium))
    }

    override fun newUiState(state: BuySellCurrencyScreenState) {
        adapter.submitList(state.currencyList)
    }

    override fun onVisibleChange(visible: Boolean) {
        super.onVisibleChange(visible)
        if (visible) {
            buySellFeature.setHeaderVisible(true)
            buySellFeature.setHeaderTitle(getString(Localization.currency))
            buySellFeature.setHeaderSubtitle(null)
            buySellFeature.data.value?.let {
                feature.setData(it)
            }
        }
    }
}