package com.tonapps.tonkeeper.ui.screen.battery.refill

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.koin.parentFragmentViewModel
import com.tonapps.tonkeeper.ui.base.BaseListWalletScreen
import com.tonapps.tonkeeper.ui.screen.battery.BatteryViewModel
import com.tonapps.tonkeeper.ui.screen.battery.refill.list.Adapter
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import uikit.R
import uikit.extensions.collectFlow
import uikit.extensions.getDimensionPixelSize

class BatteryRefillScreen : BaseListWalletScreen() {

    private val initialPromo: String? by lazy { requireArguments().getString(ARG_PROMO) }

    override val viewModel: BatteryRefillViewModel by viewModel { parametersOf(initialPromo) }

    private val parentViewModel: BatteryViewModel by parentFragmentViewModel()

    private val adapter = Adapter(
        openSettings = { parentViewModel.routeToSettings() },
        onSubmitPromo = { viewModel.applyPromo(it) }
    )

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
        if (initialPromo != null) {
            viewModel.applyPromo(initialPromo!!, true)
        }
    }

    companion object {
        private const val ARG_PROMO = "promo"

        fun newInstance() = BatteryRefillScreen()

        fun newInstance(promo: String?): BatteryRefillScreen {
            val fragment = BatteryRefillScreen()
            fragment.arguments = Bundle().apply {
                putString(ARG_PROMO, promo)
            }

            return fragment
        }
    }
}