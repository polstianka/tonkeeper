package com.tonapps.tonkeeper.ui.screen.events

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.core.history.list.HistoryAdapter
import com.tonapps.tonkeeper.core.history.list.HistoryItemDecoration
import com.tonapps.tonkeeper.core.history.list.item.HistoryItem
import com.tonapps.tonkeeper.dialog.fiat.FiatDialog
import com.tonapps.tonkeeper.ui.screen.main.MainScreen
import com.tonapps.tonkeeper.ui.screen.qr.QRScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundTransparentColor
import com.tonapps.uikit.list.ListPaginationListener
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.drawable.BarDrawable
import uikit.extensions.collectFlow
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.EmptyLayout
import uikit.widget.HeaderView

class EventsScreen: MainScreen.Child(R.layout.fragment_main_events_list) {

    private val eventsViewModel: EventsViewModel by viewModel()

    private val legacyAdapter = HistoryAdapter()
    private val paginationListener = object : ListPaginationListener() {
        override fun onLoadMore() {
            eventsViewModel.loadMore()
        }
    }

    private lateinit var headerView: HeaderView
    private lateinit var containerView: View
    private lateinit var listView: RecyclerView
    private lateinit var filtersView: RecyclerView
    private lateinit var emptyView: EmptyLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.title = getString(Localization.history)
        headerView.setColor(requireContext().backgroundTransparentColor)

        containerView = view.findViewById(R.id.container)
        filtersView = view.findViewById(R.id.filters)

        listView = view.findViewById(R.id.list)
        listView.adapter = legacyAdapter
        listView.addOnScrollListener(paginationListener)
        listView.addItemDecoration(HistoryItemDecoration)

        emptyView = view.findViewById(R.id.empty)
        emptyView.doOnButtonClick = { first ->
            if (first) {
                FiatDialog.open(requireContext())
            } else {
                openQRCode()
            }
        }
        collectFlow(eventsViewModel.isUpdatingFlow) { updating ->
            if (updating) {
                headerView.setSubtitle(Localization.updating)
            } else {
                headerView.setSubtitle(null)
            }
        }
        collectFlow(eventsViewModel.uiItemsFlow, ::setItems)
    }

    override fun scrollUp() {
        super.scrollUp()
        eventsViewModel.update()
    }

    private fun openQRCode() {
        collectFlow(eventsViewModel.openQRCode()) { walletEntity ->
            navigation?.add(QRScreen.newInstance(walletEntity.address, TokenEntity.TON, walletEntity.type))
        }
    }

    private fun setItems(items: List<HistoryItem>) {
        if (items.isEmpty()) {
            setEmptyState()
        } else {
            setListState()
            legacyAdapter.submitList(items)
        }
    }

    private fun setEmptyState() {
        if (emptyView.visibility == View.VISIBLE) {
            return
        }
        emptyView.visibility = View.VISIBLE
        containerView.visibility = View.GONE
    }

    private fun setListState() {
        if (containerView.visibility == View.VISIBLE) {
            return
        }
        emptyView.visibility = View.GONE
        containerView.visibility = View.VISIBLE
    }

    override fun getRecyclerView(): RecyclerView? {
        if (this::listView.isInitialized) {
            return listView
        }
        return null
    }

    override fun getHeaderDividerOwner(): BarDrawable.BarDrawableOwner? {
        if (this::headerView.isInitialized) {
            return headerView
        }
        return null
    }

    companion object {
        fun newInstance() = EventsScreen()
    }
}