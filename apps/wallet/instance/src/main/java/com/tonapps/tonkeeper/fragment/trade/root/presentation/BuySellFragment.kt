package com.tonapps.tonkeeper.fragment.trade.root.presentation

import android.os.Bundle
import android.util.Log
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.tonapps.tonkeeper.fragment.trade.root.vm.BuySellTabs
import com.tonapps.tonkeeper.fragment.trade.root.vm.BuySellViewModel
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uikit.base.BaseFragment
import uikit.widget.TabLayoutEx
import com.tonapps.wallet.localization.R as LocalizationR

class BuySellFragment : BaseFragment(R.layout.fragment_trade), BaseFragment.BottomSheet,
    TabLayout.OnTabSelectedListener {

    companion object {
        fun newInstance() = BuySellFragment()
    }

    private val viewModel: BuySellViewModel by viewModel()
    private val closeButton: View?
        get() = view?.findViewById(R.id.close_button_clickable_area)
    private val tabLayout: TabLayoutEx?
        get() = view?.findViewById(R.id.tab_layout)
    private val viewPager: ViewPager2?
        get() = view?.findViewById(R.id.trade_view_pager)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeButton?.setOnClickListener { finish() }
        prepareTabs()
    }

    private fun prepareTabs() {
        tabLayout?.let { tl ->
            BuySellTabs.entries
                .map { it.toTab(tl) }
                .forEach { tl.addTab(it) }
            tl.addOnTabSelectedListener(this)
        }
        viewPager?.let { vp ->
            vp.adapter = BuySellPagerAdapter(this)
            vp.isUserInputEnabled = false
            vp.disableNestedScrolling()
        }
        observeFlow(viewModel.currentTab) { viewPager?.currentItem = it.ordinal }
    }

    override fun onTabReselected(p0: TabLayout.Tab) {
        Log.wtf("###", "onTabReselected: ${p0.text}")
    }

    override fun onTabSelected(p0: TabLayout.Tab) {
        val tab = BuySellTabs.entries[p0.id]
        viewModel.onTabSelected(tab)
    }

    override fun onTabUnselected(p0: TabLayout.Tab) {
        Log.wtf("###", "onTabUnselected: ${p0.text}")
    }

    private val BuySellTabs.stringRes: Int
        get() = when (this) {
            BuySellTabs.BUY -> LocalizationR.string.buy
            BuySellTabs.SELL -> LocalizationR.string.sell
        }

    private val BuySellTabs.text: String
        get() = getString(stringRes)

    private fun BuySellTabs.toTab(tabLayout: TabLayout): TabLayout.Tab {
        return tabLayout.newTab().apply {
            id = ordinal
            text = this@toTab.text
        }
    }

    // that's the hack that allows the container bottomsheet to be swiped down
    private fun ViewPager2.disableNestedScrolling() {
        (getChildAt(0) as? RecyclerView)?.apply {
            isNestedScrollingEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }
}