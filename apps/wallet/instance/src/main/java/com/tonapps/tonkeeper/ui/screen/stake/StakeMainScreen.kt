package com.tonapps.tonkeeper.ui.screen.stake

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.viewpager2.widget.ViewPager2
import com.tonapps.tonkeeper.ui.screen.stake.options.StakeOptionsMainScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.icon.UIKitIcon
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.collectFlow
import uikit.extensions.getDimensionPixelSize
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView

class StakeMainScreen : BaseFragment(R.layout.fragment_stake_pager), BaseFragment.BottomSheet {
    private val stakeMainViewModel: StakeMainViewModel by activityViewModel()

    private lateinit var pagerView: ViewPager2
    private lateinit var headerView: HeaderView
    private lateinit var pagerAdapter: StakeScreensAdapter

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            stakeMainViewModel.setCurrentPage(position, arguments?.getBoolean(UNSTAKE_KEY) ?: false)
            if (position == 0) {
                headerView.setDefault()
                headerView.setIcon(UIKitIcon.ic_information_circle_16)
            } else {
                headerView.setIcon(UIKitIcon.ic_chevron_left_16)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pagerAdapter = StakeScreensAdapter(this, arguments?.getBoolean(UNSTAKE_KEY) ?: false)
        stakeMainViewModel.preselectedAddress = arguments?.getString(ADDRESS_KEY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pagerView = view.findViewById(R.id.pager)
        pagerView.offscreenPageLimit = 2
        pagerView.isUserInputEnabled = false
        pagerView.adapter = pagerAdapter
        pagerView.registerOnPageChangeCallback(pageChangeCallback)
        pagerView.applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium))

        headerView = view.findViewById(R.id.header)
        headerView.contentMatchParent()
        headerView.doOnActionClick = { finish() }
        headerView.doOnCloseClick = { stakeMainViewModel.onCloseClick() }

        collectFlow(stakeMainViewModel.uiState) {
            pagerView.currentItem = it.currentPage
            headerView.updateTitle(it.headerTitle)
        }

        collectFlow(stakeMainViewModel.singleEvent) {
            when (it) {
                Action.Info -> requireContext().startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://ton.org/stake"))
                )

                Action.Options -> navigation?.add(StakeOptionsMainScreen.newInstance())
                Action.Finish -> finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stakeMainViewModel.destroy()
    }

    companion object {
        private val ADDRESS_KEY = "address"
        private val UNSTAKE_KEY = "unstake"
        fun newInstance(address: String? = null, unstake: Boolean = false) =
            StakeMainScreen().apply {
                arguments = bundleOf(
                    ADDRESS_KEY to address,
                    UNSTAKE_KEY to unstake
                )
            }
    }
}