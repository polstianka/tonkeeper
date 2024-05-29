package com.tonapps.tonkeeper.fragment.staking.withdrawal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.tonapps.tonkeeper.fragment.staking.StakingScreen
import com.tonapps.tonkeeper.helper.flow.TransactionSender
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundTransparentColor
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.extensions.findFragment
import uikit.extensions.hideKeyboard
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView

class WithdrawalScreen(
    private val entity: AccountTokenEntity
) : BaseFragment(R.layout.fragment_stake), BaseFragment.BottomSheet {
    private val withdrawalScreenViewModel: WithdrawalScreenViewModel by viewModel()
    private val sender by lazy { TransactionSender.FragmentSenderController(this, withdrawalScreenViewModel.transactionSender) }

    private lateinit var headerView: HeaderView
    private lateinit var pagerView: ViewPager2
    private lateinit var pageAdapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageAdapter = Adapter(this)
        withdrawalScreenViewModel.setEntity(entity)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerView = view.findViewById(R.id.header)
        headerView.setColor(requireContext().backgroundTransparentColor)
        headerView.doOnActionClick = { finish() }
        headerView.doOnCloseClick = {
            getCurrentFocus()?.hideKeyboard()
            withdrawalScreenViewModel.prevPage()
        }

        pagerView = view.findViewById(R.id.pager)
        pagerView.offscreenPageLimit = 2
        pagerView.isUserInputEnabled = false

        pagerView.adapter = pageAdapter

        collectFlow(withdrawalScreenViewModel.pageStateFlow) { state ->
            if (state.confirmVisibility) {
                headerView.title = ""
                headerView.closeView.alpha = 1f
                pagerView.currentItem = 1
            } else {
                headerView.title = getString(Localization.unstake)
                headerView.closeView.alpha = 0f
                pagerView.currentItem = 0
            }
        }

        sender.attach { effect ->
            if (effect.navigateToHistory) {
                activity?.supportFragmentManager?.findFragment<StakingScreen>()?.finish()
                navigation?.openURL("tonkeeper://activity")
            }

            finish()
        }
    }

    companion object {
        fun newInstance(stake: AccountTokenEntity): WithdrawalScreen {
            return WithdrawalScreen(stake)
        }

        class Adapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
            private companion object {
                private var COUNT = 0
                private val POSITION_STAKE = COUNT++
                private val POSITION_OPTIONS = COUNT++
            }

            override fun getItemCount(): Int {
                return COUNT
            }

            override fun getItemId(position: Int): Long {
                return position.toLong()
            }


            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    POSITION_STAKE -> WithdrawalAmountScreen.newInstance()
                    POSITION_OPTIONS -> WithdrawalConfirmScreen.newInstance()
                    else -> throw IllegalStateException("Unknown position: $position")
                }
            }

            override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
                super.onAttachedToRecyclerView(recyclerView)
                recyclerView.isNestedScrollingEnabled = true
            }
        }
    }
}