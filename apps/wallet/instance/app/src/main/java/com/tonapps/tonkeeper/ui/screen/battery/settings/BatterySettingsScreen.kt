package com.tonapps.tonkeeper.ui.screen.battery.settings

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.koin.parentFragmentViewModel
import com.tonapps.tonkeeper.ui.base.BaseListWalletScreen
import com.tonapps.tonkeeper.ui.screen.battery.BatteryViewModel
import com.tonapps.tonkeeper.ui.screen.battery.settings.list.Adapter
import com.tonapps.tonkeeper.ui.screen.battery.settings.list.Item
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.R
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.extensions.getDimensionPixelSize

class BatterySettingsScreen: BaseListWalletScreen() {

    override val viewModel: BatterySettingsViewModel by viewModel()

    private val parentViewModel: BatteryViewModel by parentFragmentViewModel()

    private val adapter = Adapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectFlow(viewModel.uiItemsFlow) { uiItems ->
            val showTitle = !uiItems.contains(Item.SettingsHeader)
            parentViewModel.setTitle(if (showTitle) getString(Localization.transactions) else null)
            adapter.submitList(uiItems)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideHeaderContainer()
        setAdapter(adapter)
        updateListPadding(top = requireContext().getDimensionPixelSize(R.dimen.barHeight))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        parentViewModel.setTitle(null)
    }

    companion object {

        fun newInstance() = BatterySettingsScreen()
    }
}