package com.tonapps.tonkeeper.ui.screen.wallet.main

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.tonapps.tonkeeper.extensions.showToast
import com.tonapps.tonkeeper.koin.walletViewModel
import com.tonapps.tonkeeper.ui.component.MainRecyclerView
import com.tonapps.tonkeeper.ui.component.wallet.WalletHeaderView
import com.tonapps.tonkeeper.ui.screen.card.CardScreen
import com.tonapps.tonkeeper.ui.screen.card.entity.CardScreenPath
import com.tonapps.tonkeeper.ui.screen.main.MainScreen
import com.tonapps.tonkeeper.ui.screen.settings.main.SettingsScreen
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.Item.Status
import com.tonapps.tonkeeper.ui.screen.wallet.main.list.WalletAdapter
import com.tonapps.tonkeeper.ui.screen.wallet.picker.PickerScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.flow.filterNotNull
import uikit.drawable.BarDrawable
import uikit.extensions.collectFlow

class WalletScreen(wallet: WalletEntity): MainScreen.Child(R.layout.fragment_wallet, wallet) {

    override val viewModel: WalletViewModel by walletViewModel()

    private val adapter = WalletAdapter(::openCards)

    private lateinit var headerView: WalletHeaderView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var listView: MainRecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectFlow(viewModel.uiItemsFlow, adapter::submitList)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.onWalletClick = { navigation?.add(PickerScreen.newInstance()) }
        headerView.onSettingsClick = { navigation?.add(SettingsScreen.newInstance(wallet)) }
        headerView.doWalletSwipe = { right ->
            if (right) {
                viewModel.prevWallet()
            } else {
                viewModel.nextWallet()
            }
        }

        refreshLayout = view.findViewById(R.id.refresh)
        refreshLayout.setOnRefreshListener { viewModel.refresh() }

        listView = view.findViewById(R.id.list)
        listView.adapter = adapter

        collectFlow(viewModel.uiLabelFlow.filterNotNull(), headerView::setWallet)
        collectFlow(viewModel.hasBackupFlow, headerView::setDot)
        collectFlow(viewModel.statusFlow) { status ->
            if (refreshLayout.isRefreshing && status != Status.Updating) {
                refreshLayout.isRefreshing = false
            }
        }
    }

    override fun getRecyclerView(): RecyclerView? {
        if (this::listView.isInitialized) {
            return listView
        }
        return null
    }

    override fun getTopBarDrawable(): BarDrawable? {
        if (this::headerView.isInitialized) {
            return headerView.background as? BarDrawable
        }
        return null
    }

    private fun openCards(path: CardScreenPath?) {
        collectFlow(viewModel.cardsStateFlow) { cardsState ->
            if (cardsState != null) {
                navigation?.add(CardScreen.newInstance(wallet = wallet, cardsState = cardsState, path = path))
            } else {
                context?.showToast(Localization.error)
            }
        }
    }

    companion object {
        fun newInstance(wallet: WalletEntity) = WalletScreen(wallet)
    }
}