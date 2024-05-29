package com.tonapps.tonkeeper.fragment.staking.deposit.pages.options.pool

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.extensions.findParent
import com.tonapps.tonkeeper.fragment.staking.deposit.DepositScreen
import com.tonapps.tonkeeper.fragment.staking.deposit.DepositScreenViewModel
import com.tonapps.tonkeeper.ui.adapter.Adapter
import com.tonapps.tonkeeper.ui.adapter.ItemDecoration
import com.tonapps.tonkeeper.ui.component.BlurredRecyclerView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundTransparentColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.bottomScrolled
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.extensions.topScrolled
import uikit.widget.FooterViewEmpty

class StakePoolSelectScreen : BaseFragment(R.layout.fragment_stake_pool_selector_page) {
    private val poolsViewModel: DepositScreenViewModel by viewModel(ownerProducer = { this.findParent<DepositScreen>() })

    private lateinit var listView: BlurredRecyclerView
    private lateinit var footerView: FooterViewEmpty

    private lateinit var adapter: Adapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = Adapter()

        listView = view.findViewById(R.id.list)
        listView.blurredPaddingTop = 64.dp
        listView.unblurredPaddingBottom = 16.dp

        listView.addItemDecoration(ItemDecoration)
        listView.layoutManager = com.tonapps.uikit.list.LinearLayoutManager(view.context)
        listView.adapter = adapter
        listView.isNestedScrollingEnabled = true

        footerView = view.findViewById(R.id.footer)
        footerView.setColor(requireContext().backgroundTransparentColor)

        collectFlow(listView.topScrolled, poolsViewModel::setHeaderDividerPoolsList)
        collectFlow(listView.bottomScrolled, footerView::setDivider)

        collectFlow(poolsViewModel.poolUiItemsFlow) { items ->
            adapter.submitList(items)

        }
    }

    companion object {
        fun newInstance() = StakePoolSelectScreen()
    }
}