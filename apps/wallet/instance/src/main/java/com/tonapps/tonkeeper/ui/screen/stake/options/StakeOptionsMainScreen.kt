package com.tonapps.tonkeeper.ui.screen.stake.options

import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.icon.UIKitIcon
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.widget.HeaderView

class StakeOptionsMainScreen : BaseFragment(R.layout.fragment_stake_pager),
    BaseFragment.BottomSheet {
    private val optionsViewModel: StakeOptionsMainViewModel by activityViewModel()

    private lateinit var pagerView: ViewPager2
    private lateinit var headerView: HeaderView
    private lateinit var pagerAdapter: StakeOptionsScreensAdapter

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            optionsViewModel.setCurrentPage(position)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pagerAdapter = StakeOptionsScreensAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pagerView = view.findViewById(R.id.pager)
        pagerView.offscreenPageLimit = 3
        pagerView.isUserInputEnabled = false
        pagerView.adapter = pagerAdapter
        pagerView.registerOnPageChangeCallback(pageChangeCallback)

        headerView = view.findViewById(R.id.header)
        headerView.contentMatchParent()
        headerView.doOnActionClick = { finish() }
        headerView.doOnCloseClick = { optionsViewModel.onPrevPage() }
        headerView.setIcon(UIKitIcon.ic_chevron_left_16)

        collectFlow(optionsViewModel.uiState) {
            pagerView.currentItem = it.currentPage
            headerView.updateTitle(it.headerTitle)
        }

        collectFlow(optionsViewModel.singleEvent) {
            when (it) {
                Action.Finish -> finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        optionsViewModel.destroy()
    }

    companion object {
        fun newInstance() = StakeOptionsMainScreen()
    }
}