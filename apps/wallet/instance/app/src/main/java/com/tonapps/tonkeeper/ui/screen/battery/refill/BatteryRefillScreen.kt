package com.tonapps.tonkeeper.ui.screen.battery.refill

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.koin.walletViewModel
import com.tonapps.tonkeeper.ui.base.BaseHolderWalletScreen
import com.tonapps.tonkeeper.ui.base.ScreenContext
import com.tonapps.tonkeeper.ui.screen.battery.BatteryScreen
import com.tonapps.tonkeeper.ui.screen.battery.BatteryViewModel
import com.tonapps.tonkeeper.ui.screen.battery.refill.list.Adapter
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.wallet.data.account.entities.WalletEntity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import uikit.R
import uikit.extensions.collectFlow
import uikit.extensions.getDimensionPixelSize

class BatteryRefillScreen(wallet: WalletEntity) : BaseHolderWalletScreen.ChildListScreen<ScreenContext.Wallet, BatteryScreen, BatteryViewModel>(ScreenContext.Wallet(wallet)) {

    private val initialPromo: String? by lazy { requireArguments().getString(ARG_PROMO) }

    override val viewModel: BatteryRefillViewModel by walletViewModel()

    private val adapter = Adapter(
        openSettings = { primaryViewModel.routeToSettings() },
        onSubmitPromo = { viewModel.applyPromo(it) },
        onPackSelect = { viewModel.makePurchase(requireActivity(), it) },
        onRestorePurchases = { viewModel.restorePurchases() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectFlow(viewModel.uiItemsFlow, adapter::submitList)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeaderBackground(R.drawable.bg_page_gradient)
        setActionIcon(UIKitIcon.ic_close_16) { finish() }
        setAdapter(adapter)
        if (initialPromo != null) {
            viewModel.applyPromo(initialPromo!!, true)
        }
    }

    companion object {
        private const val ARG_PROMO = "promo"

        fun newInstance(wallet: WalletEntity) = BatteryRefillScreen(wallet)

        fun newInstance(wallet: WalletEntity, promo: String?): BatteryRefillScreen {
            val fragment = BatteryRefillScreen(wallet)
            fragment.putStringArg(ARG_PROMO, promo)
            return fragment
        }
    }
}