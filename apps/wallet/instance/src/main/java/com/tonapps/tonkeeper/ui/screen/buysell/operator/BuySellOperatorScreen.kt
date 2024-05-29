package com.tonapps.tonkeeper.ui.screen.buysell.operator

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.ui.screen.buysell.pager.BuySellScreenAdapter
import com.tonapps.tonkeeper.ui.screen.buysell.pager.PagerScreen
import com.tonapps.tonkeeper.ui.screen.settings.currency.CurrencyViewModel
import com.tonapps.tonkeeper.ui.screen.swap.ListBackgroundDecoration
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.widget.SimpleRecyclerView

class BuySellOperatorScreen :
    PagerScreen<BuySellOperatorScreenState, BuySellOperatorScreenEffect, BuySellOperatorScreenFeature>(
        R.layout.fragment_buysell_operator
    ) {

    companion object {
        fun newInstance() = BuySellOperatorScreen()
    }

    override val feature: BuySellOperatorScreenFeature by viewModel()

    private val adapter: BuySellOperatorAdapter by lazy {
        BuySellOperatorAdapter {
            feature.selectOperator(it)
        }
    }

    private lateinit var continueButton: Button
    private lateinit var typeList: SimpleRecyclerView
    private lateinit var currencyName: AppCompatTextView
    private lateinit var currencyCode: AppCompatTextView
    private lateinit var currencyContainer: View
    private lateinit var empty: View
    private lateinit var skeleton: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        continueButton = view.findViewById(R.id.continue_action)
        continueButton.setOnClickListener {
            buySellFeature.setOperator(feature.uiState.value.selectedOperator)
            buySellFeature.setCurrency(feature.uiState.value.currency)
            buySellFeature.setCurrentPage(BuySellScreenAdapter.POSITION_CONFIRM)
        }

        skeleton = view.findViewById(R.id.skeleton)

        typeList = view.findViewById(R.id.typeList)
        typeList.adapter = adapter

        currencyContainer = view.findViewById(R.id.currency_container)
        currencyContainer.setOnClickListener {
            buySellFeature.setCurrentPage(BuySellScreenAdapter.POSITION_CURRENCY)
        }

        currencyCode = view.findViewById(R.id.currency_code)
        currencyName = view.findViewById(R.id.currency_name)
        empty = view.findViewById(R.id.empty)

        buySellFeature.data.observe(viewLifecycleOwner) { data ->
            feature.setCurrency(data.currency)
        }
    }

    override fun newUiState(state: BuySellOperatorScreenState) {
        adapter.submitList(state.operators)
        continueButton.isEnabled = state.selectedOperator != null
        if (state.loading) {
            skeleton.alpha = 1f
            skeleton.isVisible = true
            typeList.isVisible = false
            empty.isVisible = false
        } else {
            if (skeleton.isVisible && skeleton.alpha == 1f) {
                skeleton.animate().cancel()
                skeleton.animate().alpha(0f).setDuration(500).start()
                if (state.operators.isNotEmpty()) {
                    empty.isVisible = false
                    typeList.isVisible = true
                    typeList.alpha = 0f
                    typeList.animate().cancel()
                    typeList.animate().alpha(1f).setDuration(500).start()
                } else {
                    typeList.isVisible = false
                    empty.isVisible = true
                    empty.alpha = 0f
                    empty.animate().cancel()
                    empty.animate().alpha(1f).setDuration(500).start()
                }
            } else {
                skeleton.isVisible = false
                empty.isVisible = state.operators.isEmpty()
                typeList.isVisible = state.operators.isNotEmpty()
            }
        }
        currencyCode.text = state.currency.code
        currencyName.text = getString(CurrencyViewModel.getNameResIdForCurrency(state.currency.code))
    }

    override fun onVisibleChange(visible: Boolean) {
        super.onVisibleChange(visible)
        if (visible) {
            buySellFeature.setHeaderVisible(true)
            buySellFeature.setHeaderTitle(getString(Localization.operator))
            buySellFeature.setHeaderSubtitle(buySellFeature.data.value?.buySellType?.title)
            buySellFeature.data.value?.let {
                feature.setData(it)
            }
        }
    }
}