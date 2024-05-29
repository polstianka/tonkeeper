package com.tonapps.tonkeeper.ui.screen.buyOrSell

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.operator.OperatorScreen
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.BuyOrSellViewModel
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.ProgressButtonBuyOrSellState
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.DealState
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.RatesModel.RatesModel
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.formatNumber
import com.tonapps.tonkeeper.ui.screen.buyOrSell.view.ListPayMethodAdapter
import com.tonapps.tonkeeper.ui.screen.swap.view.progressButton.ProgressButton
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.ListCell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uikit.base.BaseFragment
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.SimpleRecyclerView

class BasicBuyOrSellScreen(
    private val state: DealState,
    private val nextNavHandler: (selectedTon: Double) -> Unit
) : Fragment(R.layout.fragment_basic_buy_or_sell),
    BaseFragment.BottomSheet {

    private val viewModel: BuyOrSellViewModel by activityViewModels()

    private lateinit var inputFieldValue: AppCompatEditText
    private lateinit var txCurUsdPrice: TextView
    private lateinit var progressButton: ProgressButton
    private lateinit var list: SimpleRecyclerView

    private val adapter = ListPayMethodAdapter()

    private var _stateButtonEnterAmount =
        MutableStateFlow<ProgressButtonBuyOrSellState>(ProgressButtonBuyOrSellState.EnterAmount)

    private val stateButtonEnterAmount get() = _stateButtonEnterAmount

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // init views
        inputFieldValue = view.findViewById(R.id.inputFieldValue)
        txCurUsdPrice = view.findViewById(R.id.txCurUsdPrice)
        progressButton = view.findViewById(R.id.progressButton)
        list = view.findViewById(R.id.list)
        list.adapter = adapter
        fillTypeMethod()



        progressButton.setUpLoading(false)
        progressButton.buttonClickListener = {
            nextNavHandler(
                inputFieldValue.text.toString().toDouble()
            )
        }

        lifecycleScope.launch {
            val stateRateBuy = launch {
                viewModel.rateBuy.collectLatest { rateModel ->
                    if (rateModel != null) {
                        updateCurrencyTx(
                            rateModel = rateModel,
                            inputFieldValue.text.toString().toDouble()
                        )
                    }
                }
            }

            val stateButtonEnterAmount = launch {
                stateButtonEnterAmount.collectLatest {
                    when (it) {
                        ProgressButtonBuyOrSellState.EnterAmount -> {
                            progressButton.updateStateEnabledButton(false)
                            progressButton.updateTextInButton("Enter Amount")
                        }

                        ProgressButtonBuyOrSellState.EnteredAmountIsBelowMinimum -> {
                            progressButton.updateStateEnabledButton(false)
                            progressButton.updateTextInButton("Entered amount is below the minimum")
                        }

                        ProgressButtonBuyOrSellState.Continue -> {
                            progressButton.updateStateEnabledButton(true)
                            progressButton.updateTextInButton("Continue")
                        }
                    }
                }
            }

            stateRateBuy.join()
            stateButtonEnterAmount.join()
        }
        inputFieldValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().toDoubleOrNull() != null) {
                    viewModel.rateBuy.value?.let {
                        updateCurrencyTx(
                            rateModel = it,
                            s.toString().toDouble()
                        )
                    }
                } else {
                    viewModel.rateBuy.value?.let {
                        updateCurrencyTx(
                            rateModel = it,
                            0.0
                        )
                    }
                    inputFieldValue.setText("0")
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateCurrencyTx(rateModel: RatesModel, itemInputPrice: Double) {
        if (itemInputPrice == 0.0) {
            txCurUsdPrice.text = "${
                itemInputPrice.toString().formatNumber()
            } ${rateModel.itemRates?.get(0)?.currency}"
            updateStateEnterAmount(ProgressButtonBuyOrSellState.EnterAmount)
        } else {
            if (rateModel.itemRates?.get(0)?.rate != null) {
                val inputItem =
                    (itemInputPrice * rateModel.itemRates[0].rate).toString().formatNumber()
                txCurUsdPrice.text =
                    "${inputItem.toString().take(5)} ${rateModel.itemRates?.get(0)?.currency}"
                if (itemInputPrice < 50) {
                    updateStateEnterAmount(ProgressButtonBuyOrSellState.EnteredAmountIsBelowMinimum)
                } else {
                    updateStateEnterAmount(ProgressButtonBuyOrSellState.Continue)
                }
            }
        }
    }


    private fun updateStateEnterAmount(newState: ProgressButtonBuyOrSellState) {
        _stateButtonEnterAmount.update { newState }
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun fillTypeMethod() {
        val modelList = mutableListOf<Item.ListPayMethod>()
        modelList.add(Item.ListPayMethod(name = "Credit Card", image_url = resources.getDrawable(R.drawable.pay_method_1), position = ListCell.Position.FIRST ))
        modelList.add(Item.ListPayMethod(name = "Credit Card  ·  RUB", image_url = resources.getDrawable(R.drawable.pay_method_2),  position = ListCell.Position.MIDDLE ))
        modelList.add(Item.ListPayMethod(name = "Cryptocurrency", image_url = resources.getDrawable(R.drawable.pay_method_3) ,  position = ListCell.Position.MIDDLE))
        modelList.add(Item.ListPayMethod(name = "Apple Pay", image_url = resources.getDrawable(R.drawable.pay_method_4) ,  position = ListCell.Position.LAST))
        adapter.submitList(modelList)
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateDealState(state)
    }

}