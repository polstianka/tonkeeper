package com.tonapps.tonkeeper.dialog.trade

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.dialog.trade.list.PaymentTypesAdapter
import com.tonapps.tonkeeper.dialog.trade.operator.ChooseOperatorFragment
import com.tonapps.tonkeeperx.R
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import uikit.base.BaseFragment
import uikit.extensions.doKeyboardAnimation
import uikit.navigation.Navigation.Companion.navigation

class TradeDialog : BaseFragment(R.layout.fragment_trade), BaseFragment.BottomSheet {
    private lateinit var continueButton: Button
    private lateinit var paymentMethodsList: RecyclerView
    private lateinit var currencyEdit: EditText
    private lateinit var conversionText: TextView
    private lateinit var minAmountText: TextView
    private lateinit var headerView: TradeHeaderView
    private val tradeViewModel by activityViewModel<TradeViewModel>()
    private val adapter =
        PaymentTypesAdapter {
            tradeViewModel.selectPaymentType(it.paymentType)
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        continueButton = view.findViewById(R.id.continue_button)
        paymentMethodsList = view.findViewById(R.id.payment_methods_list)
        currencyEdit = view.findViewById(R.id.currency_edit)
        conversionText = view.findViewById(R.id.conversion_text)
        minAmountText = view.findViewById(R.id.min_amount_text)
        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }
        headerView.doOnTabClick = {
            tradeViewModel.changeBuyMode(it)
        }
        paymentMethodsList.adapter = adapter
        paymentMethodsList.layoutManager =
            com.tonapps.uikit.list.LinearLayoutManager(requireContext())
        view.doKeyboardAnimation { offset, _, _ ->
            continueButton.translationY = -offset.toFloat()
        }

        continueButton.setOnClickListener {
            navigation?.add(ChooseOperatorFragment.newInstance())
        }
        currencyEdit.addTextChangedListener {
            it.toString().toDoubleOrNull()?.let { amount ->
                tradeViewModel.updateAmount(amount)
            }
        }
        collectViewModelFlows()
    }

    private fun collectViewModelFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                tradeViewModel
                    .minAmountFlow
                    .onEach {
                        currencyEdit.setText(it)
                        minAmountText.text = getString(com.tonapps.wallet.localization.R.string.min_amount, it)
                    }.launchIn(this)
                tradeViewModel
                    .paymentItemsFlow
                    .onEach {
                        adapter.submit(it)
                    }.launchIn(this)
                tradeViewModel
                    .convertedFlow
                    .onEach {
                        conversionText.text = it
                    }.launchIn(this)
                tradeViewModel
                    .isContinueButtonActiveFlow
                    .onEach {
                        continueButton.isEnabled = it
                    }.launchIn(this)
            }
        }
    }
}
