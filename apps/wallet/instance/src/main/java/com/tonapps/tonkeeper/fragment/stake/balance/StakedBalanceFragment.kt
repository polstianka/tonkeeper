package com.tonapps.tonkeeper.fragment.stake.balance

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.fragment.jetton.JettonScreen
import com.tonapps.tonkeeper.fragment.stake.confirm.ConfirmStakeFragment
import com.tonapps.tonkeeper.fragment.stake.domain.StakingTransactionType
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakedBalance
import com.tonapps.tonkeeper.fragment.stake.domain.model.getAvailableCryptoBalance
import com.tonapps.tonkeeper.fragment.stake.domain.model.getAvailableFiatBalance
import com.tonapps.tonkeeper.fragment.stake.domain.model.getPendingStakeBalance
import com.tonapps.tonkeeper.fragment.stake.domain.model.getPendingStakeBalanceFiat
import com.tonapps.tonkeeper.fragment.stake.domain.model.getPendingUnstakeBalance
import com.tonapps.tonkeeper.fragment.stake.domain.model.getPendingUnstakeBalanceFiat
import com.tonapps.tonkeeper.fragment.stake.domain.model.getUnstakeReadyBalance
import com.tonapps.tonkeeper.fragment.stake.domain.model.getUnstakeReadyBalanceFiat
import com.tonapps.tonkeeper.fragment.stake.domain.model.hasPendingStake
import com.tonapps.tonkeeper.fragment.stake.domain.model.hasPendingUnstake
import com.tonapps.tonkeeper.fragment.stake.domain.model.hasUnstakeReady
import com.tonapps.tonkeeper.fragment.stake.presentation.getIconUri
import com.tonapps.tonkeeper.fragment.stake.root.StakeFragment
import com.tonapps.tonkeeper.fragment.stake.ui.LiquidStakingDetailsView
import com.tonapps.tonkeeper.fragment.stake.ui.PoolDetailsView
import com.tonapps.tonkeeper.fragment.stake.ui.PoolLinksView
import com.tonapps.tonkeeper.fragment.stake.unstake.UnstakeFragment
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applySelectableBgContent
import uikit.extensions.setThrottleClickListener
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView

class StakedBalanceFragment : BaseFragment(
    R.layout.fragment_staked_balance
), BaseFragment.SwipeBack {

    companion object {
        fun newInstance(
            stakedBalance: StakedBalance
        ) = StakedBalanceFragment().apply {
            setArgs(
                StakedBalanceArgs(stakedBalance)
            )
        }
    }

    private val viewModel: StakedBalanceViewModel by viewModel()
    private val header: HeaderView?
        get() = view?.findViewById(R.id.fragment_staked_balance_header)
    private val balanceCrypto: TextView?
        get() = view?.findViewById(R.id.fragment_staked_balance_balance_crypto)
    private val balanceFiat: TextView?
        get() = view?.findViewById(R.id.fragment_staked_balance_balance_fiat)
    private val iconBig: SimpleDraweeView?
        get() = view?.findViewById(R.id.fragment_staked_balance_icon_big)
    private val iconSmall: SimpleDraweeView?
        get() = view?.findViewById(R.id.fragment_staked_balance_icon_small)
    private val stakeButton: View?
        get() = view?.findViewById(R.id.fragment_staked_balance_stake_button)
    private val unstakeButton: View?
        get() = view?.findViewById(R.id.fragment_staked_balance_unstake_button)
    private val liquidStakingDetailsView: LiquidStakingDetailsView?
        get() = view?.findViewById(R.id.fragment_staked_balance_liquid_staking_details)
    private val poolDetailsView: PoolDetailsView?
        get() = view?.findViewById(R.id.fragment_staked_balance_pool_details)
    private val poolLinksView: PoolLinksView?
        get() = view?.findViewById(R.id.fragment_staked_balance_links)
    private val pendingStakeGroup: View?
        get() = view?.findViewById(R.id.fragment_staked_balance_pending_stake_group)
    private val pendingUnstakeGroup: View?
        get() = view?.findViewById(R.id.fragment_staked_balance_pending_unstake_group)
    private val pendingUnstakeCrypto: TextView?
        get() = view?.findViewById(R.id.fragment_staked_balance_pending_unstake_amount_crypto)
    private val pendingStakeCrypto: TextView?
        get() = view?.findViewById(R.id.fragment_staked_balance_pending_stake_amount_crypto)
    private val pendingUnstakeFiat: TextView?
        get() = view?.findViewById(R.id.fragment_staked_balance_pending_unstake_amount_fiat)
    private val pendingStakeFiat: TextView?
        get() = view?.findViewById(R.id.fragment_staked_balance_pending_stake_amount_fiat)
    private val unstakeReadyGroup: View?
        get() = view?.findViewById(R.id.fragment_staked_balance_unstake_ready_group)
    private val unstakeReadyCrypto: TextView?
        get() = view?.findViewById(R.id.fragment_staked_balance_unstake_ready_amount_crypto)
    private val unstakeReadyFiat: TextView?
        get() = view?.findViewById(R.id.fragment_staked_balance_unstake_ready_amount_fiat)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.provideArgs(
                StakedBalanceArgs(
                    requireArguments()
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header?.doOnCloseClick = { viewModel.onCloseClicked() }

        stakeButton?.setThrottleClickListener { viewModel.onStakeClicked() }

        unstakeButton?.setThrottleClickListener { viewModel.onUnstakeClicked() }

        poolLinksView?.setOnChipClicked { viewModel.onChipClicked(it) }

        liquidStakingDetailsView?.setOnTokenAreaClickListener { viewModel.onTokenClicked() }

        observeFlow(viewModel.events) { handleEvent(it) }
        observeFlow(viewModel.args) { updateState(it) }
        observeFlow(viewModel.jetton) { liquidStakingDetailsView?.applyLiquidJetton(it) }
        observeFlow(viewModel.chips) { poolLinksView?.applyChips(it) }
    }

    private fun updateState(args: StakedBalanceArgs) {
        header?.title = args.stakedBalance.pool.name
        balanceCrypto?.text = CurrencyFormatter.format(
            "TON",
            args.stakedBalance.getAvailableCryptoBalance()
        )
        balanceFiat?.text = CurrencyFormatter.format(
            args.stakedBalance.fiatCurrency.code,
            args.stakedBalance.getAvailableFiatBalance()
        )
        iconBig?.setImageResource(com.tonapps.wallet.api.R.drawable.ic_ton_with_bg)
        iconSmall?.setImageURI(args.stakedBalance.pool.serviceType.getIconUri())
        poolDetailsView?.setPool(args.stakedBalance.pool)
        unstakeReadyGroup?.applySelectableBgContent()
        unstakeReadyGroup?.setThrottleClickListener { viewModel.onUnstakeReadyClicked() }

        updatePendingDepositView(args.stakedBalance)
        updatePendingWithdrawView(args.stakedBalance)
        updateUnstakeReadyView(args.stakedBalance)
        args.stakedBalance.liquidBalance?.let {
            liquidStakingDetailsView?.setAmount(it.asset.balance, it.assetRate)
        }
    }

    private fun updateUnstakeReadyView(stakedBalance: StakedBalance) {
        val isVisible = stakedBalance.hasUnstakeReady()
        unstakeReadyGroup?.isVisible = isVisible
        if (!isVisible) return
        unstakeReadyCrypto?.text = CurrencyFormatter.format(
            "TON",
            stakedBalance.getUnstakeReadyBalance()
        )
        unstakeReadyFiat?.text = CurrencyFormatter.format(
            stakedBalance.fiatCurrency.code,
            stakedBalance.getUnstakeReadyBalanceFiat()
        )
    }

    private fun updatePendingWithdrawView(stakedBalance: StakedBalance) {
        val isVisible = stakedBalance.hasPendingUnstake()
        pendingUnstakeGroup?.isVisible = isVisible
        if (!isVisible) return
        pendingUnstakeCrypto?.text = CurrencyFormatter.format(
            "TON",
            stakedBalance.getPendingUnstakeBalance()
        )
        pendingUnstakeFiat?.text = CurrencyFormatter.format(
            stakedBalance.fiatCurrency.code,
            stakedBalance.getPendingUnstakeBalanceFiat()
        )
    }

    private fun updatePendingDepositView(stakedBalance: StakedBalance) {
        val isVisible = stakedBalance.hasPendingStake()
        pendingStakeGroup?.isVisible = isVisible
        if (!isVisible) return
        pendingStakeCrypto?.text = CurrencyFormatter.format(
            "TON",
            stakedBalance.getPendingStakeBalance()
        )
        pendingStakeFiat?.text = CurrencyFormatter.format(
            stakedBalance.fiatCurrency.code,
            stakedBalance.getPendingStakeBalanceFiat()
        )
    }

    private fun handleEvent(event: StakedBalanceEvent) {
        when (event) {
            StakedBalanceEvent.NavigateBack -> finish()
            is StakedBalanceEvent.NavigateToStake -> event.handle()
            is StakedBalanceEvent.NavigateToLink -> navigation?.openURL(event.url, true)
            is StakedBalanceEvent.NavigateToToken -> event.handle()
        }
    }

    private fun StakedBalanceEvent.NavigateToToken.handle() {
        val fragment = JettonScreen.newInstance(
            asset.contractAddress,
            asset.displayName,
            asset.symbol
        )
        navigation?.add(fragment)
    }

    private fun StakedBalanceEvent.NavigateToStake.handle() {
        val fragment = when (stakingDirection) {
            StakingTransactionType.DEPOSIT -> StakeFragment.newInstance(
                balance.pool,
                balance.service
            )
            StakingTransactionType.UNSTAKE -> UnstakeFragment.newInstance(balance)
            StakingTransactionType.UNSTAKE_CONFIRM -> ConfirmStakeFragment.newInstance(
                balance.pool,
                balance.getUnstakeReadyBalance(),
                StakingTransactionType.UNSTAKE_CONFIRM,
                false
            )
        }
        navigation?.add(fragment)
    }
}