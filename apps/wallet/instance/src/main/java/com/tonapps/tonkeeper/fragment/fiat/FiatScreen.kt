package com.tonapps.tonkeeper.fragment.fiat

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.fragment.country.CountryScreen
import com.tonapps.tonkeeper.fragment.fiat.dialog.ConfirmationDialog
import com.tonapps.tonkeeper.fragment.fiat.web.FiatWebFragment
import com.tonapps.tonkeeper.ui.adapter.Adapter
import com.tonapps.tonkeeper.ui.adapter.ItemDecoration
import com.tonapps.tonkeeper.ui.component.BlurredRecyclerView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundTransparentColor
import com.tonapps.uikit.color.iconTertiaryColor
import com.tonapps.uikit.color.textPrimaryColor
import com.tonapps.uikit.color.textSecondaryColor
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.drawable.TabDrawable
import uikit.extensions.bottomScrolled
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.extensions.hideKeyboard
import uikit.extensions.topScrolled
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FooterViewEmpty
import uikit.widget.HeaderViewEmpty
import uikit.widget.item.ItemIconView

class FiatScreen : BaseFragment(R.layout.fragment_fiat), BaseFragment.BottomSheet {
    private val fiatScreenViewModel: FiatScreenViewModel by viewModel()

    private lateinit var closeView: AppCompatImageView
    private lateinit var countryButton: AppCompatTextView

    private lateinit var buttonBuy: AppCompatTextView
    private lateinit var buttonSell: AppCompatTextView
    private lateinit var buttonBuyDrawable: TabDrawable
    private lateinit var buttonSellDrawable: TabDrawable

    private lateinit var currencyView: ItemIconView
    private lateinit var nextButton: Button

    private val adapter = Adapter()

    private lateinit var headerView: HeaderViewEmpty
    private lateinit var footerView: FooterViewEmpty
    private lateinit var listView: BlurredRecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerView = view.findViewById(R.id.header)
        headerView.setColor(requireContext().backgroundTransparentColor)

        footerView = view.findViewById(R.id.footer)
        footerView.setColor(requireContext().backgroundTransparentColor)

        buttonSellDrawable = TabDrawable(requireContext())
        buttonBuyDrawable = TabDrawable(requireContext())
        buttonBuyDrawable.setActive(active = true, animated = false)

        view.findViewById<View>(R.id.button_buy_wrapper).background = buttonBuyDrawable
        view.findViewById<View>(R.id.button_sell_wrapper).background = buttonSellDrawable

        buttonBuy = view.findViewById(R.id.button_buy)
        buttonBuy.setOnClickListener {
            fiatScreenViewModel.setAction(FiatScreenViewModel.Action.Buy)
        }

        buttonSell = view.findViewById(R.id.button_sell)
        buttonSell.setOnClickListener {
            fiatScreenViewModel.setAction(FiatScreenViewModel.Action.Sell)
        }




        closeView = view.findViewById(R.id.header_close)
        closeView.setOnClickListener {
            getCurrentFocus()?.hideKeyboard()
            finish()
        }

        countryButton = view.findViewById(R.id.country)
        countryButton.setOnClickListener {
            navigation?.add(CountryScreen.newInstance(FIAT_DIALOG_REQUEST))
        }

        listView = view.findViewById(R.id.list)
        listView.addItemDecoration(ItemDecoration)
        listView.blurredPaddingTop = 64.dp
        listView.blurredPaddingBottom = 160.dp

        listView.adapter = adapter
        listView.layoutManager = com.tonapps.uikit.list.LinearLayoutManager(requireContext())

        currencyView = view.findViewById(R.id.currency)
        currencyView.iconRes = com.tonapps.uikit.icon.R.drawable.ic_switch_16
        currencyView.setIconTintColor(requireContext().iconTertiaryColor)

        currencyView.setOnClickListener {
            navigation?.add(CurrencyScreen.newInstance(this))
        }

        nextButton = view.findViewById(R.id.next)

        collectFlow(listView.topScrolled, headerView::setDivider)
        collectFlow(listView.bottomScrolled, footerView::setDivider)

        collectFlow(fiatScreenViewModel.selectedActionFlow) {
            when (it) {
                FiatScreenViewModel.Action.Buy -> {
                    buttonBuy.setTextColor(requireContext().textPrimaryColor)
                    buttonBuyDrawable.setActive(true)
                    buttonSell.setTextColor(requireContext().textSecondaryColor)
                    buttonSellDrawable.setActive(false)
                }

                FiatScreenViewModel.Action.Sell -> {
                    buttonBuy.setTextColor(requireContext().textSecondaryColor)
                    buttonBuyDrawable.setActive(false)
                    buttonSell.setTextColor(requireContext().textPrimaryColor)
                    buttonSellDrawable.setActive(true)
                }
            }
        }

        collectFlow(fiatScreenViewModel.selectedCurrencyFlow) {
            currencyView.text = it.code
            currencyView.description = getString(it.name)
        }

        collectFlow(fiatScreenViewModel.settingsRepository.countryFlow) {
            countryButton.text = it
        }

        collectFlow(fiatScreenViewModel.uiItemsFlow) {
            adapter.submitList(it)
        }

        collectFlow(fiatScreenViewModel.selectedMethodFlow) { method ->
            nextButton.setOnClickListener {
                openItem(method)
            }
        }
    }

    private val confirmationDialog: ConfirmationDialog by lazy {
        ConfirmationDialog(requireContext())
    }

    private fun openItem(item: FiatScreenViewModel.Method) {
        lifecycleScope.launch {
            if (isShowConfirmation(item.method.id)) {
                showConfirmDialog(item)
            } else {
                openUrl(item)
            }
        }
    }

    private suspend fun isShowConfirmation(
        id: String
    ): Boolean {
        return App.fiat.isShowConfirmation(id)
    }

    private fun disableShowConfirmation(id: String) {
        lifecycleScope.launch {
            App.fiat.disableShowConfirmation(id)
        }
    }

    private fun showConfirmDialog(item: FiatScreenViewModel.Method) {
        confirmationDialog.show(item.method) { disableConfirm ->
            openUrl(item)
            if (disableConfirm) {
                disableShowConfirmation(item.method.id)
            }
        }
    }

    private fun openUrl(
        item: FiatScreenViewModel.Method
    ) {
        finish()
        val pattern = item.method.successUrlPattern

        lifecycleScope.launch {
            navigation?.add(FiatWebFragment.newInstance(item.getUrl(), pattern))
        }
    }

    override fun getViewForNestedScrolling(): View {
        return listView
    }

    override fun onDragging() {
        super.onDragging()
        getCurrentFocus()?.hideKeyboard()
    }

    companion object {
        const val FIAT_DIALOG_REQUEST = "fiat_dialog_request"

        fun newInstance() = FiatScreen()
    }
}