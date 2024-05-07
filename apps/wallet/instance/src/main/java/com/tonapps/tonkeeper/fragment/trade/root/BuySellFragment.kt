package com.tonapps.tonkeeper.fragment.trade.root

import android.os.Bundle
import android.util.Log
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.view.View
import com.google.android.material.tabs.TabLayout
import com.tonapps.tonkeeper.fragment.trade.root.vm.BuySellTabs
import com.tonapps.tonkeeper.fragment.trade.root.vm.BuySellViewModel
import com.tonapps.tonkeeperx.R
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeButton?.setOnClickListener { finish() }
        tabLayout?.let { tl ->
            BuySellTabs.entries
                .map { it.toTab(tl) }
                .forEach { tl.addTab(it) }
            tl.addOnTabSelectedListener(this)
        }
    }

    override fun onTabReselected(p0: TabLayout.Tab) {
        Log.wtf("###", "onTabReselected: ${p0.text}")
    }

    override fun onTabSelected(p0: TabLayout.Tab) {
        Log.wtf("###", "onTabSelected: ${p0.text}")
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
}