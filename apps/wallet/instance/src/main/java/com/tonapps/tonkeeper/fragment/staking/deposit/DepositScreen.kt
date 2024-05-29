package com.tonapps.tonkeeper.fragment.staking.deposit

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.tonapps.tonkeeper.fragment.fiat.web.FiatWebFragment
import com.tonapps.tonkeeper.fragment.staking.StakingScreen
import com.tonapps.tonkeeper.fragment.staking.deposit.pages.options.OptionsPagerScreen
import com.tonapps.tonkeeper.fragment.staking.deposit.pages.stake.StakePagerScreen
import com.tonapps.tonkeeper.fragment.staking.deposit.view.StakeFrameLayout
import com.tonapps.tonkeeper.helper.flow.TransactionSender
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundTransparentColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.extensions.findFragment
import uikit.extensions.hideKeyboard
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView

class DepositScreen : BaseFragment(R.layout.fragment_stake), BaseFragment.BottomSheet {
    private val poolsViewModel: DepositScreenViewModel by viewModel()
    private val sender by lazy { TransactionSender.FragmentSenderController(this, poolsViewModel.transactionSender) }

    private lateinit var headerView: HeaderView
    private lateinit var pagerView: ViewPager2
    private lateinit var pageAdapter: Adapter

    private lateinit var fakeView: StakeFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageAdapter = Adapter(this)

        poolsViewModel.init(defaultPoolAddress)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerView = view.findViewById(R.id.header)
        headerView.setColor(requireContext().backgroundTransparentColor)
        headerView.contentMatchParent()
        headerView.doOnActionClick = { finish() }

        pagerView = view.findViewById(R.id.pager)
        pagerView.offscreenPageLimit = 2
        pagerView.isUserInputEnabled = false
        pagerView.adapter = pageAdapter

        fakeView = view.findViewById(R.id.wrapper)

        collectFlow(poolsViewModel.pageStateFlow) { state ->
            headerView.setDivider(state.headerDivider)
            pagerView.currentItem = if (state.selectorVisibility) 1 else 0

            if (state.showInformationIcon) {
                headerView.closeView.setImageResource(com.tonapps.uikit.icon.R.drawable.ic_information_circle_16)
                headerView.doOnCloseClick = {
                    finish()
                    navigation?.add(FiatWebFragment.newInstance("https://ton.org/stake", null))
                }
            } else {
                headerView.closeView.setImageResource(com.tonapps.uikit.icon.R.drawable.ic_chevron_left_16)
                headerView.doOnCloseClick = {
                    getCurrentFocus()?.hideKeyboard()
                    poolsViewModel.prevPage()
                }
            }
        }

        collectFlow(poolsViewModel.headerTextFlow) { text ->
            headerView.title = text
        }

        sender.attach { effect ->
            if (effect.navigateToHistory) {
                activity?.supportFragmentManager?.findFragment<StakingScreen>()?.finish()
                navigation?.openURL("tonkeeper://activity")
            }

            finish()
        }
    }

    override fun onDragging() {
        super.onDragging()
        getCurrentFocus()?.hideKeyboard()
    }

    private val defaultPoolAddress: String? by lazy {
        arguments?.getString(POOL_ADDRESS_KEY)
    }

    companion object {
        private const val POOL_ADDRESS_KEY = "POOL_ADDRESS_KEY"

        fun newInstance(): DepositScreen = newInstance(null)

        fun newInstance(poolAddress: String?): DepositScreen {
            val screen = DepositScreen()
            screen.arguments = Bundle().apply {
                putString(POOL_ADDRESS_KEY, poolAddress)
            }
            return screen
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
                    POSITION_STAKE -> StakePagerScreen.newInstance()
                    POSITION_OPTIONS -> OptionsPagerScreen.newInstance()
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