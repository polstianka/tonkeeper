package com.tonapps.tonkeeper.ui.screen.buysell.operator

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.core.fiat.models.FiatItem
import com.tonapps.tonkeeper.ui.screen.buysell.confirm.BuySellConfirmScreen
import com.tonapps.tonkeeper.ui.screen.buysell.currency.BuySellCurrencyScreen
import com.tonapps.tonkeeper.ui.screen.buysell.main.BuySellListener
import com.tonapps.tonkeeper.ui.screen.buysell.operator.list.OperatorMethodAdapter
import com.tonapps.tonkeeper.ui.screen.buysell.operator.list.OperatorMethodItem
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.rates.entity.OperatorBuyRateEntity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView

class OperatorScreen : BaseFragment(R.layout.fragment_operator), BaseFragment.BottomSheet,
    OperatorListener {

    private val viewModel: OperatorViewModel by viewModel()

    private val args: OperatorArgs by lazy { OperatorArgs(requireArguments()) }

    private var buySellListener: BuySellListener? = null

    lateinit var headerView: HeaderView
    lateinit var currencyContainer: LinearLayoutCompat
    lateinit var currencyCode: AppCompatTextView
    lateinit var currencyName: AppCompatTextView
    lateinit var listView: RecyclerView

    private val adapter = OperatorMethodAdapter {
        openConfirm(it)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        handleViews()
        handleViewModel()

    }

    private fun initializeViews(view: View) {

        headerView = view.findViewById(R.id.header)
        currencyContainer = view.findViewById(R.id.currency_container)
        currencyCode = view.findViewById(R.id.currency_code)
        currencyName = view.findViewById(R.id.currency_name)

        listView = view.findViewById(R.id.list)!!
        listView.adapter = adapter
        listView.layoutManager = com.tonapps.uikit.list.LinearLayoutManager(requireContext())

    }

    private fun handleViews() {

        headerView.doOnActionClick = { finish() }

        currencyContainer.setOnClickListener {
            navigation?.add(BuySellCurrencyScreen.newInstance())
        }

    }

    private fun handleViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.screenStateFlow.collect { screenState ->
                newUiState(screenState)
            }
        }
    }

    private fun newUiState(screenState: OperatorScreenState) = screenState.apply {
        currencyCode.text = currency?.code
        currencyNameResId?.also { resId ->
            currencyName.text = getString(resId)
        }

        showWithData(fiatItems, operatorRates)
    }

    private fun showWithData(
        items: List<FiatItem>,
        operatorRateslist: Map<String, OperatorBuyRateEntity>
    ) {

        adapter.submitList(OperatorMethodAdapter.buildMethodItems(items, operatorRateslist)) {
            // fixPeekHeight()
        }
    }

    fun setBuySellListener(buySellListener: BuySellListener) {
        this.buySellListener = buySellListener
    }

    private fun openConfirm(item: OperatorMethodItem) {
        BuySellConfirmScreen.newInstance(args.amount, item.rate, item.body).also {
            it.setOpListener(this)
            navigation?.add(it)
        }
    }

    companion object {

        fun newInstance(inputAmount: Double): OperatorScreen {
            val fragment = OperatorScreen()
            fragment.arguments = OperatorArgs(inputAmount).toBundle()
            return fragment
        }
    }

    override fun onDismiss() {
        buySellListener?.onDismiss()
        finish()
    }

}