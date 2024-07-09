package com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.operator

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.currencylist.CurrencyListScreen
import com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.lastCheck.LastCheckArgs
import com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.lastCheck.LastCheckScreen
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.BuyOrSellViewModel
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.DealState
import com.tonapps.tonkeeper.ui.screen.swap.view.progressButton.ProgressButton
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeperx.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uikit.base.BaseFragment
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView
import uikit.widget.SimpleRecyclerView

class OperatorScreen : BaseFragment(R.layout.fragment_operator), BaseFragment.BottomSheet {

    private val args: OperatorArgs by lazy { OperatorArgs(requireArguments()) }

    private lateinit var header: HeaderView
    private lateinit var cardCurrency: MaterialCardView
    private lateinit var txCountryCode: AppCompatTextView
    private lateinit var txCurrency: AppCompatTextView
    private lateinit var list: SimpleRecyclerView
    private lateinit var progressButton: ProgressButton

    private val viewModel: BuyOrSellViewModel by activityViewModels()
    val adapter by lazy {
        OperatorAdapter() {
            viewModel.updateSelectedPayMethod(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // init view
        header = view.findViewById(R.id.header)
        cardCurrency = view.findViewById(R.id.cardCurrency)
        txCountryCode = view.findViewById(R.id.countryCode)
        txCurrency = view.findViewById(R.id.currency)
        list = view.findViewById(R.id.list)
        progressButton = view.findViewById(R.id.progressButton)
        progressButton.setUpLoading(false)
        progressButton.updateStateEnabledButton(true)
        progressButton.updateTextInButton("Continue")
        //
        list.adapter = adapter
        adapter.submitList(listOf(Item.Skeleton()))
        header.doOnActionClick = {
            finish()
        }
        lifecycleScope.launch {
            val stateSelectedFiat = launch {
                viewModel.selectedFiat.collectLatest {
                    if(viewModel.stateDeal.value == DealState.BUY) {
                        viewModel.getPaymentOperatorMethods(args.selectedTon)
                    } else {
                        viewModel.getPaymentOperatorMethodsSell(args.selectedTon)
                    }
                    if (it.layoutByCountry != null) {
                        txCountryCode.text = it.layoutByCountry.countryCode
                        txCurrency.text = it.layoutByCountry.currency
                    }
                }
            }
            val statePaymentOperatorMethod = launch {
                viewModel.statePaymentOperatorMethod.collectLatest {

                    if (it.isNotEmpty()) {
                        Log.d("statePaymentOperatorMethod", "called - $it")
                        adapter.submitList(it)
                    }
                }
            }

            stateSelectedFiat.join()
            statePaymentOperatorMethod.join()
        }
        progressButton.buttonClickListener = {
            val permResult = viewModel.searchFiatPayMethod()
            val stateItemSelectedPayMethod = viewModel.stateItemSelectedPayMethod.value
            if (permResult != null && stateItemSelectedPayMethod != null) {
                if(viewModel.stateDeal.value == DealState.BUY) {
                    viewModel.selectedFiat.value.layoutByCountry?.let {
                        LastCheckScreen.newInstance(
                            courseRate = stateItemSelectedPayMethod.courseRate,
                            sendAmount = args.selectedTon,
                            sendCurrencyNm = "TON",
                            receiveCurrencyNm = it.currency,
                            countryNm = it.countryCode.uppercase(),
                            address = args.address
                        )
                    }?.let {
                        navigation?.add(
                            it
                        )
                    }
                } else {
                    viewModel.selectedFiat.value.layoutByCountry?.let {
                        LastCheckScreen.newInstance(
                            courseRate = stateItemSelectedPayMethod.courseRate,
                            sendAmount = args.selectedTon,
                            sendCurrencyNm = "USD",
                            receiveCurrencyNm = "TON",
                            countryNm = it.countryCode.uppercase(),
                            address = args.address
                        )
                    }?.let {
                        navigation?.add(
                            it
                        )
                    }
                }
            }
        }

        cardCurrency.setOnClickListener {
            viewModel.fiatList.value?.data?.let {
                CurrencyListScreen.newInstance(
                    it.layoutByCountry
                )
            }?.let {
                navigation?.add(
                    it
                )
            }
        }
    }

    companion object {

        fun newInstance(
            selectedTon: Double,
            address: String
        ): OperatorScreen {
            val fragment = OperatorScreen()
            fragment.arguments = OperatorArgs(selectedTon = selectedTon, address = address).toBundle()
            return fragment
        }
    }

}