package com.tonapps.tonkeeper.ui.screen.buysell.amount


import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.api.buysell.TradeType
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.ui.screen.buysell.pager.BuySellScreenAdapter
import com.tonapps.tonkeeper.ui.screen.buysell.pager.PagerScreen
import com.tonapps.tonkeeper.ui.screen.swap.ListBackgroundDecoration
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.constantRedColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.uikit.color.textTertiaryColor
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.extensions.focusWithKeyboard
import uikit.extensions.hideKeyboard
import uikit.widget.SimpleRecyclerView

class BuySellAmountScreen :
    PagerScreen<BuySellAmountScreenState, BuySellAmountScreenEffect, BuySellAmountScreenFeature>(R.layout.fragment_buysell_amount) {

    companion object {
        fun newInstance() = BuySellAmountScreen()
    }

    override val feature: BuySellAmountScreenFeature by viewModel()

    private val adapter: BuySellTypeAdapter by lazy {
        BuySellTypeAdapter {
            feature.selectType(it)
        }
    }

    private lateinit var valueView: AmountInput
    private lateinit var valueCurrencyView: AppCompatTextView
    private lateinit var rateView: AppCompatTextView
    private lateinit var minAmount: AppCompatTextView
    private lateinit var continueButton: Button
    private lateinit var typeList: SimpleRecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        valueView = view.findViewById(R.id.value)
        valueView.doOnTextChanged { _, _, _, _ ->
            feature.setValue(getValue())
        }

        valueCurrencyView = view.findViewById(R.id.value_currency)

        rateView = view.findViewById(R.id.rate)
        minAmount = view.findViewById(R.id.min_amount)

        continueButton = view.findViewById(R.id.continue_action)
        continueButton.setOnClickListener { next() }

        feature.setValue(0f)

        typeList = view.findViewById(R.id.typeList)
        typeList.adapter = adapter

        buySellFeature.data.observe(viewLifecycleOwner) { data ->
            feature.setCurrency(data.currency)
        }
    }

    fun forceSetAmount(amount: Float) {
        val text = if (0f >= amount) {
            ""
        } else {
            amount.toString()
        }
        val editable = valueView.text ?: return
        editable.replace(0, editable.length, text)
    }

    private fun getValue(): Float {
        return valueView.text.toString().toFloatOrNull() ?: 0f
    }

    private fun next() {
        buySellFeature.setType(feature.uiState.value.selectedType)
        buySellFeature.setTradeType(feature.uiState.value.tradeType)
        buySellFeature.setAmount(valueView.text.toString().toFloatOrNull() ?: 0f)
        buySellFeature.setCryptoBalance(feature.currentBalance)
        buySellFeature.setCurrentPage(BuySellScreenAdapter.POSITION_OPERATOR)
    }

    fun setTradeType(tradeType: TradeType) {
        feature.setTradeType(tradeType)
    }

    override fun newUiState(state: BuySellAmountScreenState) {
        rateView.text = state.rate
        valueView.setDecimalCount(state.decimals)
        valueCurrencyView.text = state.selectedTokenCode
        continueButton.isEnabled = state.canContinue
        adapter.submitList(state.types)

        if (state.insufficientBalance) {
            minAmount.setText(Localization.insufficient_balance)
            minAmount.setTextColor(requireContext().constantRedColor)
        } else {
            minAmount.text = getString(Localization.minimum_amount, "${state.minAmount} ${state.selectedTokenCode}")
            minAmount.setTextColor(requireContext().textTertiaryColor)
        }

    }

    override fun onVisibleChange(visible: Boolean) {
        super.onVisibleChange(visible)
        if (visible) {
            buySellFeature.setHeaderVisible(false)
            valueView.focusWithKeyboard()
        } else {
            valueView.hideKeyboard()
        }
    }
}