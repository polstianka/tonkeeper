package com.tonapps.tonkeeper.dialog.trade.currencybottomsheet

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.ui.screen.settings.currency.CurrencyViewModel
import com.tonapps.tonkeeper.ui.screen.settings.currency.list.Adapter
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.collectFlow
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.topScrolled
import uikit.widget.HeaderView

class CurrencyBottomSheetScreen :
    BaseFragment(uikit.R.layout.fragment_list),
    BaseFragment.BottomSheet {
    private val currencyViewModel: CurrencyViewModel by viewModel()

    private val adapter = Adapter(::selectCurrency)

    private lateinit var headerView: HeaderView
    private lateinit var listView: RecyclerView

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(uikit.R.id.header)
        headerView.setAction(com.tonapps.uikit.icon.R.drawable.ic_close_16)
        headerView.doOnActionClick = {
            finish()
        }
        listView = view.findViewById(uikit.R.id.list)
        listView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.cornerMedium))

        collectFlow(listView.topScrolled, headerView::setDivider)
        setTitle(getString(Localization.currency))
        setAdapter(adapter)

        collectFlow(currencyViewModel.uiItemsFlow, adapter::submitList)
    }

    private fun setTitle(title: String) {
        headerView.title = title
    }

    private fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        listView.adapter = adapter
    }

    private fun selectCurrency(currency: String) {
        currencyViewModel.selectCurrency(currency)
        finish()
    }

    companion object {
        fun newInstance() = CurrencyBottomSheetScreen()
    }
}
