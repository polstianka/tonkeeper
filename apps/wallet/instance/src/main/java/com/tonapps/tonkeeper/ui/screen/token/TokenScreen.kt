package com.tonapps.tonkeeper.ui.screen.token

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ConcatAdapter
import com.tonapps.tonkeeper.core.history.list.HistoryAdapter
import com.tonapps.tonkeeper.core.history.list.HistoryItemDecoration
import com.tonapps.tonkeeper.popup.ActionSheet
import com.tonapps.tonkeeper.ui.screen.browser.dapp.DAppScreen
import com.tonapps.tonkeeper.ui.screen.token.list.TokenAdapter
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.uikit.list.ListPaginationListener
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import uikit.base.BaseFragment
import uikit.base.BaseListFragment
import uikit.extensions.collectFlow
import uikit.navigation.Navigation.Companion.navigation

class TokenScreen: BaseListFragment(), BaseFragment.SwipeBack {

    private val args: TokenArgs by lazy { TokenArgs(requireArguments()) }
    private val tokenViewModel: TokenViewModel by viewModel { parametersOf(args.address) }
    private val tokenAdapter = TokenAdapter()
    private val historyAdapter = HistoryAdapter()
    private val paginationListener = object : ListPaginationListener() {
        override fun onLoadMore() {
            tokenViewModel.loadMore()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle(args.symbol)
        setAdapter(ConcatAdapter(tokenAdapter, historyAdapter))
        addItemDecoration(HistoryItemDecoration)
        addScrollListener(paginationListener)
        collectFlow(tokenViewModel.tokenFlow, ::applyToken)
        collectFlow(tokenViewModel.uiItemsFlow, tokenAdapter::submitList)
        collectFlow(tokenViewModel.uiHistoryFlow, historyAdapter::submitList)
    }

    private fun applyToken(token: TokenData) {
        setActionIcon(R.drawable.ic_ellipsis_16) { actionMenu(it, token) }
    }

    private fun actionMenu(view: View, token: TokenData) {
        val actionSheet = ActionSheet(requireContext())
        actionSheet.addItem(0, Localization.view_details, R.drawable.ic_globe_16)
        actionSheet.doOnItemClick = { navigation?.openURL(token.detailsUrl.toString(), true) }
        actionSheet.show(view)
    }

    companion object {
        fun newInstance(address: String, name: String, symbol: String): TokenScreen {
            val fragment = TokenScreen()
            fragment.setArgs(TokenArgs(address, name, symbol))
            return fragment
        }
    }
}