package com.tonapps.tonkeeper.fragment.staking.deposit.pages.options.pool

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeper.api.percentage
import com.tonapps.tonkeeper.extensions.findParent
import com.tonapps.tonkeeper.fragment.staking.deposit.DepositScreen
import com.tonapps.tonkeeper.fragment.staking.deposit.DepositScreenViewModel
import com.tonapps.tonkeeper.fragment.staking.deposit.view.LinksView
import com.tonapps.tonkeeper.fragment.swap.view.SwapInfoRowView
import com.tonapps.tonkeeper.helper.Coin2
import com.tonapps.tonkeeper.ui.component.BlurredScrollView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundTransparentColor
import com.tonapps.wallet.api.entity.TokenEntity
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.bottomScrolled
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.widget.FooterViewEmpty

class StakePoolSelectConfirmScreen :
    BaseFragment(R.layout.fragment_stake_pool_selector_confirm_page) {
    private val poolsViewModel: DepositScreenViewModel by viewModel(ownerProducer = { this.findParent<DepositScreen>() })

    private lateinit var linksView: LinksView

    private lateinit var depositRowView: SwapInfoRowView
    private lateinit var apyRowView: SwapInfoRowView
    private lateinit var nextButton: Button

    private lateinit var listView: BlurredScrollView
    private lateinit var footerView: FooterViewEmpty

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = view.findViewById(R.id.list)
        listView.blurDisabled = true
        listView.blurredPaddingTop = 64.dp
        listView.blurredPaddingBottom = 88.dp
        listView.isNestedScrollingEnabled = true

        footerView = view.findViewById(R.id.footer)
        footerView.setColor(requireContext().backgroundTransparentColor)

        collectFlow(listView.bottomScrolled, footerView::setDivider)

        depositRowView = view.findViewById(R.id.row_deposit)
        apyRowView = view.findViewById(R.id.row_apy)
        linksView = view.findViewById(R.id.links)
        nextButton = view.findViewById(R.id.next)

        combine(poolsViewModel.poolsFlow, poolsViewModel.selectedPoolFlow) { pools, pool ->
            depositRowView.setValue(
                Coin2.fromNano(pool.minStake)
                    .toString(TokenEntity.TON.decimals) + " " + TokenEntity.TON.symbol
            )

            apyRowView.setValue("≈ " + pool.apy.percentage)
            apyRowView.labelView.isVisible = pools.maxApyPool == pool
            linksView.setLinks(pool.links)

            nextButton.setOnClickListener {
                poolsViewModel.onPoolConfirm(pool)
            }
        }.launchIn(lifecycleScope)
    }

    companion object {
        fun newInstance() = StakePoolSelectConfirmScreen()
    }
}
