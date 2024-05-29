package com.tonapps.tonkeeper.fragment.fiat

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.ui.component.BlurredRecyclerView
import com.tonapps.tonkeeper.ui.screen.settings.currency.list.Adapter
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundTransparentColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.extensions.topScrolled
import uikit.widget.FooterViewEmpty
import uikit.widget.HeaderView

class CurrencyScreen(
    fiatScreen: FiatScreen
) : BaseFragment(R.layout.fragment_fiat_currency), BaseFragment.BottomSheet {

    private val currencyViewModel: FiatScreenViewModel by viewModel(ownerProducer = { fiatScreen })

    private lateinit var headerView: HeaderView
    private lateinit var footerView: FooterViewEmpty
    private lateinit var listView: BlurredRecyclerView

    private val adapter = Adapter(::selectCurrency)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerView = view.findViewById(R.id.header)
        headerView.setColor(requireContext().backgroundTransparentColor)

        footerView = view.findViewById(R.id.footer)
        footerView.setColor(requireContext().backgroundTransparentColor)

        listView = view.findViewById(R.id.list)
        listView.blurredPaddingTop = 64.dp
        listView.unblurredPaddingBottom = 16.dp
        listView.adapter = adapter

        collectFlow(listView.topScrolled, headerView::setDivider)
        collectFlow(currencyViewModel.currencyUiItemsFlow, adapter::submitList)
    }

    private fun selectCurrency(currency: String) {
        currencyViewModel.setCurrency(currency)
        finish()
    }

    override fun getViewForNestedScrolling(): View {
        return listView
    }

    companion object {
        fun newInstance(fiatScreen: FiatScreen) = CurrencyScreen(fiatScreen)
    }
}