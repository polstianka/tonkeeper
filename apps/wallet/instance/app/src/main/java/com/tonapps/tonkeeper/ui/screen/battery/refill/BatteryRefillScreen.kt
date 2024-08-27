package com.tonapps.tonkeeper.ui.screen.battery.refill

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.koin.parentFragmentViewModel
import com.tonapps.tonkeeper.ui.base.BaseListWalletScreen
import com.tonapps.tonkeeper.ui.screen.battery.BatteryViewModel
import com.tonapps.tonkeeper.ui.screen.battery.refill.list.Adapter
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.R
import uikit.extensions.collectFlow
import uikit.extensions.getDimensionPixelSize

class BatteryRefillScreen: BaseListWalletScreen() {

    override val viewModel: BatteryRefillViewModel by viewModel()

    private val parentViewModel: BatteryViewModel by parentFragmentViewModel()

    private val adapter = Adapter {
        parentViewModel.routeToSettings()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentViewModel.setTitle(null)
        collectFlow(viewModel.uiItemsFlow, adapter::submitList)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideHeaderContainer()
        setAdapter(adapter)
        updateListPadding(top = requireContext().getDimensionPixelSize(R.dimen.barHeight))
    }

    companion object {

        fun newInstance() = BatteryRefillScreen()
    }
}