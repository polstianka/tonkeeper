package com.tonapps.tonkeeper.dialog.trade.operator

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.core.fiat.models.FiatItem
import com.tonapps.tonkeeper.core.fiat.models.FiatSuccessUrlPattern
import com.tonapps.tonkeeper.dialog.fiat.ConfirmationDialog
import com.tonapps.tonkeeper.dialog.trade.TradeViewModel
import com.tonapps.tonkeeper.dialog.trade.currencybottomsheet.CurrencyBottomSheetScreen
import com.tonapps.tonkeeper.dialog.trade.operator.confirmation.ConfirmationTradeScreen
import com.tonapps.tonkeeper.dialog.trade.operator.list.OperatorAdapter
import com.tonapps.tonkeeper.fragment.country.CountryScreen
import com.tonapps.tonkeeper.ui.screen.settings.currency.CurrencyViewModel
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.LinearLayoutManager
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.doKeyboardAnimation
import uikit.extensions.invisible
import uikit.extensions.visible
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView
import uikit.widget.RowLayout

private const val CHOOSE_OPERATOR_REQUEST = "choose_operator"

class ChooseOperatorFragment :
    BaseFragment(R.layout.fragment_choose_operator),
    BaseFragment.BottomSheet {
    companion object {
        fun newInstance(): ChooseOperatorFragment {
            return ChooseOperatorFragment()
        }
    }

    private val confirmationDialog: ConfirmationDialog by lazy {
        ConfirmationDialog(requireContext())
    }

    private val adapter =
        OperatorAdapter {
            chooseOperatorViewModel.checkItem(it.id)
        }

    private lateinit var headerView: HeaderView
    private lateinit var listView: RecyclerView
    private lateinit var continueButton: Button
    private lateinit var currencySelector: RowLayout
    private lateinit var currencyCodeText: TextView
    private lateinit var currencyNameText: TextView
    private lateinit var errorText: TextView
    private val currencyViewModel by viewModel<CurrencyViewModel>()
    private val chooseOperatorViewModel by viewModel<ChooseOperatorViewModel>()
    private val tradeViewModel by activityViewModel<TradeViewModel>()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        headerView = view.findViewById(R.id.header)!!
        continueButton = view.findViewById(R.id.continue_button)
        currencySelector = view.findViewById(R.id.currency_select_layout)
        currencyCodeText = view.findViewById(R.id.currency_code_text)
        currencyNameText = view.findViewById(R.id.currency_name_text)
        errorText = view.findViewById(R.id.error_text)
        headerView.setBackgroundColor(Color.TRANSPARENT)
        headerView.doOnCloseClick = { finish() }
        headerView.doOnActionClick = { finish() }
        headerView.setSubtitle(com.tonapps.wallet.localization.R.string.credit_card)
        listView = view.findViewById(R.id.list)!!
        listView.adapter = adapter
        view.doKeyboardAnimation { offset, _, _ ->
            continueButton.translationY = -offset.toFloat()
        }
        currencySelector.setOnClickListener {
            navigation?.add(CurrencyBottomSheetScreen())
        }
        continueButton.setOnClickListener {
            chooseOperatorViewModel.onContinueButtonClicked()
        }
        listView.layoutManager = LinearLayoutManager(requireContext())
        collectViewModelFlows()
    }

    private fun collectViewModelFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                currencyViewModel
                    .uiItemsFlow
                    .map { items ->
                        items.filter { item -> item.selected }
                    }
                    .filter { it.isNotEmpty() }
                    .onEach {
                        val item = it[0]
                        currencyCodeText.text = item.currency
                        currencyNameText.text = getString(item.nameResId)
                        chooseOperatorViewModel.load(
                            tradeViewModel.isBuyMode.value,
                            currency = item.currency,
                        )
                    }.launchIn(this)
                chooseOperatorViewModel
                    .itemsFlow
                    .onEach { items ->
                        showWithData(items) // TODO remove mapping
                    }.launchIn(this)
                chooseOperatorViewModel
                    .isLoadingFlow
                    .onEach { isLoading ->
                        if (isLoading) {
                            headerView.startLoading()
                        } else {
                            headerView.stopLoading()
                        }
                    }.launchIn(this)
                chooseOperatorViewModel
                    .showConfirmationScreenFlow
                    .onEach { item ->
                        showConfirmationScreen(item)
                    }.launchIn(this)
                chooseOperatorViewModel
                    .showConfirmDialogFlow
                    .onEach { item ->
                        showConfirmDialog(item)
                    }.launchIn(this)
                chooseOperatorViewModel
                    .openUrlFlow
                    .onEach {
                        openUrl(it.first, it.second)
                    }.launchIn(this)
                chooseOperatorViewModel.itemsPresentFlow
                    .drop(1)
                    .onEach { isActive ->
                        continueButton.isEnabled = isActive
                        if (isActive) {
                            errorText.invisible(animate = true)
                            listView.visible(animate = true)
                        } else {
                            errorText.visible(animate = true)
                            listView.invisible(animate = true)
                        }
                    }.launchIn(this)
            }
        }
    }

    private fun showWithData(items: List<OperatorItem>) {
        adapter.submitList(OperatorAdapter.buildMethodItems(items))
    }

    private fun disableShowConfirmation(id: String) {
        lifecycleScope.launch {
            App.fiat.disableShowConfirmation(id)
        }
    }

    private fun showConfirmationScreen(operatorItem: OperatorItem) {
        navigation?.add(
            ConfirmationTradeScreen.newInstance(
                operatorItem,
            ),
        )
    }

    private fun showConfirmDialog(fiatItem: FiatItem) {
        confirmationDialog.show(fiatItem) { disableConfirm ->
            openUrl(fiatItem.actionButton.url, fiatItem.successUrlPattern)
            if (disableConfirm) {
                disableShowConfirmation(fiatItem.id)
            }
        }
    }

    private fun openUrl(
        url: String,
        pattern: FiatSuccessUrlPattern?,
    ) {
        // TODO Fix
        navigation?.add(ConfirmationTradeScreen())
        /*finish()
        navigation?.add(FiatWebFragment.newInstance(url, pattern))*/
    }

    private fun pickCountry() {
        finish()
        navigation?.add(CountryScreen.newInstance(CHOOSE_OPERATOR_REQUEST))
    }
}
