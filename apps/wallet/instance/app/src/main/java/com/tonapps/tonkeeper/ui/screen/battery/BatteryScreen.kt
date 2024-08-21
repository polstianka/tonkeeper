package com.tonapps.tonkeeper.ui.screen.battery

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.ui.base.BaseWalletScreen
import com.tonapps.tonkeeper.ui.screen.battery.list.Adapter
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.settings.BatteryTransaction
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.collectFlow
import uikit.widget.HeaderView
import uikit.widget.SlideBetweenView

class BatteryScreen : BaseWalletScreen(R.layout.fragment_battery_screen), BaseFragment.BottomSheet {

    override val viewModel: BatteryViewModel by viewModel()

    private val refillAdapter = Adapter(::showSettings)
    private val settingsAdapter = Adapter(::showSettings)

    private lateinit var headerView: HeaderView
    private lateinit var slidesView: SlideBetweenView
    private lateinit var refillListView: RecyclerView
    private lateinit var settingsListView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectFlow(viewModel.uiItemsFlow, refillAdapter::submitList)
        collectFlow(viewModel.uiSettingsItemsFlow, settingsAdapter::submitList)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        slidesView = view.findViewById(R.id.slides)
        headerView = view.findViewById(R.id.header)

        headerView.setBackgroundResource(uikit.R.drawable.bg_page_gradient)
        headerView.doOnActionClick = { finish() }
        headerView.doOnCloseClick = { showRefill() }

        headerView.closeView.visibility = View.GONE

        refillListView = view.findViewById(R.id.refill_list)
        refillListView.adapter = refillAdapter
        refillListView.applyNavBottomPadding()

        settingsListView = view.findViewById(R.id.settings_list)
        settingsListView.adapter = settingsAdapter
        settingsListView.applyNavBottomPadding()
    }

    override fun onBackPressed(): Boolean {
        if (slidesView.getCurrentIndex() > 0) {
            showRefill()
            return false
        }

        return super.onBackPressed()
    }

    private fun showRefill() {
        headerView.closeView.visibility = View.GONE
        slidesView.prev()
    }

    private fun showSettings() {
        headerView.closeView.visibility = View.VISIBLE
        slidesView.next()
    }

    companion object {
        fun newInstance() = BatteryScreen()
    }
}