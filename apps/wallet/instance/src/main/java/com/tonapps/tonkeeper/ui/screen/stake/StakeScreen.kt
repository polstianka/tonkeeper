package com.tonapps.tonkeeper.ui.screen.stake

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.tonapps.tonkeeper.fragment.send.SendScreen
import com.tonapps.tonkeeper.ui.screen.stake.pager.PagerScreen
import com.tonapps.tonkeeper.ui.screen.stake.pager.StakeScreenAdapter
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.icon.UIKitIcon
import uikit.base.BaseFragment
import uikit.extensions.hideKeyboard
import uikit.mvi.UiScreen
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView

class StakeScreen : UiScreen<StakeScreenState, StakeScreenEffect, StakeScreenFeature>(R.layout.fragment_stake), BaseFragment.BottomSheet {

    companion object {

        private const val ADDRESS_KEY = "address"
        private const val UNSTAKE_KEY = "unstake"

        fun newInstance(address: String? = null, unstake: Boolean = false): StakeScreen {
            val fragment = StakeScreen()
            fragment.arguments = Bundle().apply {
                putString(ADDRESS_KEY, address)
                putBoolean(UNSTAKE_KEY, unstake)
            }
            return fragment
        }
    }

    override val feature: StakeScreenFeature by viewModels()
    private val address: String by lazy { arguments?.getString(ADDRESS_KEY) ?: "" }
    private val unstake: Boolean by lazy { arguments?.getBoolean(UNSTAKE_KEY, false) ?: false }
    private lateinit var pageAdapter: StakeScreenAdapter
    private var fromPage = 0

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            feature.setCurrentPage(position)
            if (position == 0) {
                headerView.setDefault()
                if (feature.data.value?.preUnstake != true) {
                    headerView.setIcon(R.drawable.information_circle_16)
                }
            } else {
                headerView.setIcon(UIKitIcon.ic_chevron_left_16)
            }
            for (i in 0 until pageAdapter.itemCount) {
                val fragment = pageAdapter.findFragmentByPosition(i) as? PagerScreen<*, *, *>
                fragment?.visible = i == position
                fragment?.view?.alpha = if (i == position || i == fromPage) 1f else 0f
            }
        }
    }

    private lateinit var headerView: HeaderView
    private lateinit var pagerView: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageAdapter = StakeScreenAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.contentMatchParent()
        headerView.doOnActionClick = { finish() }
        headerView.doOnCloseClick = {
            when (pagerView.currentItem) {
                StakeScreenAdapter.POSITION_AMOUNT -> {
                    navigation?.openURL("https://ton.org/stake", true)
                }
                StakeScreenAdapter.POSITION_CONFIRM -> feature.prevPage()
                StakeScreenAdapter.POSITION_OPTIONS -> {
                    if (pageAdapter.stakeOptionsScreen?.onBack() == true) {
                        feature.setCurrentPage(0)
                    }
                }
                StakeScreenAdapter.POSITION_CHOOSE -> feature.prevPage()
            }
        }

        pagerView = view.findViewById(R.id.pager)
        pagerView.offscreenPageLimit = 3
        pagerView.isUserInputEnabled = false
        pagerView.adapter = pageAdapter
        pagerView.registerOnPageChangeCallback(pageChangeCallback)

        if (address.isNotEmpty()) {
            feature.setPreData(address, unstake)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pagerView.unregisterOnPageChangeCallback(pageChangeCallback)
    }

    override fun onEndShowingAnimation() {
        super.onEndShowingAnimation()
        feature.readyView()
    }

    override fun onDragging() {
        super.onDragging()
        getCurrentFocus()?.hideKeyboard()
    }

    override fun newUiState(state: StakeScreenState) {
        fromPage = pagerView.currentItem
        pagerView.currentItem = state.currentPage
        headerView.title = state.headerTitle
        if (state.headerVisible) {
            headerView.showText()
        } else {
            headerView.hideText()
        }
    }

    override fun newUiEffect(effect: StakeScreenEffect) {
        super.newUiEffect(effect)

        if (effect is StakeScreenEffect.Finish) {
            finish()
        }
    }
}