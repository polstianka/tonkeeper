package com.tonapps.tonkeeper.ui.screen.purchase

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.core.entities.WalletPurchaseMethodEntity
import com.tonapps.tonkeeper.extensions.countryEmoji
import com.tonapps.tonkeeper.helper.BrowserHelper
import com.tonapps.tonkeeper.koin.walletViewModel
import com.tonapps.tonkeeper.ui.base.WalletContextScreen
import com.tonapps.tonkeeper.ui.screen.country.CountryPickerScreen
import com.tonapps.tonkeeper.ui.screen.purchase.list.Adapter
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.purchase.entity.PurchaseMethodEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import org.koin.android.ext.android.inject
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.collectFlow
import uikit.navigation.NavigationActivity

class PurchaseScreen(wallet: WalletEntity): WalletContextScreen(R.layout.fragment_purchase, wallet), BaseFragment.BottomSheet {

    override val viewModel: PurchaseViewModel by walletViewModel()

    private val settingsRepository: SettingsRepository by inject()
    private val api: API by inject()

    private val adapter: Adapter by lazy { Adapter(::open) }
    private val confirmDialog: PurchaseConfirmDialog by lazy {
        PurchaseConfirmDialog(requireContext())
    }

    private lateinit var countryView: AppCompatTextView
    private lateinit var tabBuyView: AppCompatTextView
    private lateinit var tabSellView: AppCompatTextView
    private lateinit var listView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectFlow(viewModel.uiItemsFlow, adapter::submitList)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        countryView = view.findViewById(R.id.country)
        countryView.setOnClickListener {
            navigation?.add(CountryPickerScreen.newInstance(COUNTRY_REQUEST_KEY))
        }

        tabBuyView = view.findViewById(R.id.tab_buy)
        tabBuyView.setOnClickListener { viewModel.setTab(PurchaseViewModel.Tab.BUY) }

        tabSellView = view.findViewById(R.id.tab_sell)
        tabSellView.setOnClickListener { viewModel.setTab(PurchaseViewModel.Tab.SELL) }

        listView = view.findViewById(R.id.list)
        listView.adapter = adapter
        listView.applyNavBottomPadding()

        view.findViewById<View>(R.id.close).setOnClickListener { finish() }

        collectFlow(viewModel.countryFlow) { country ->
            countryView.text = country.countryEmoji
        }

        collectFlow(viewModel.tabFlow, ::applyTab)
    }

    private fun applyTab(tab: PurchaseViewModel.Tab) {
        if (tab == PurchaseViewModel.Tab.BUY) {
            tabBuyView.setBackgroundResource(uikit.R.drawable.bg_button_tertiary)
            tabSellView.background = null
        } else if (tab == PurchaseViewModel.Tab.SELL) {
            tabBuyView.background = null
            tabSellView.setBackgroundResource(uikit.R.drawable.bg_button_tertiary)
        }
    }

    private fun open(method: PurchaseMethodEntity) {
        val activity = requireActivity() as NavigationActivity
        val methodWrapped = WalletPurchaseMethodEntity(
            method = method,
            wallet = screenContext.wallet,
            currency = settingsRepository.currency.code,
            config = api.config
        )
        if (viewModel.isPurchaseOpenConfirm(method)) {
            confirmDialog.show(method) { showAgain ->
                if (!showAgain) {
                    viewModel.disableConfirmDialog(screenContext.wallet, method)
                }
                BrowserHelper.openPurchase(activity, methodWrapped)
            }
        } else {
            BrowserHelper.openPurchase(activity, methodWrapped)
        }
    }

    companion object {
        private const val COUNTRY_REQUEST_KEY = "buy_country_request"

        fun newInstance(wallet: WalletEntity) = PurchaseScreen(wallet)
    }
}