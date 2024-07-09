package com.tonapps.tonkeeper.ui.screen.buyOrSell.utils

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.DealState
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.RatesModel.ItemRates
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.RatesModel.RatesModel
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.SelectedFiatModel
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel.FiatModel
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel.LayoutByCountry
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.uikit.list.ListCell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BuyOrSellViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val context = application
    private val repository = BuyOrSellRepository(context)

    private var _fiatList = MutableStateFlow<FiatModel?>(null)
    val fiatList get() = _fiatList

    private var _stateDeal = MutableStateFlow<DealState>(DealState.BUY)
    val stateDeal get() = _stateDeal

    private var _selectedFiat = MutableStateFlow<SelectedFiatModel>(SelectedFiatModel("", null))
    val selectedFiat get() = _selectedFiat

    private var _rateBuy = MutableStateFlow<RatesModel?>(null)
    val rateBuy get() = _rateBuy

    private var _rateSell = MutableStateFlow<RatesModel?>(null)
    val rateSell get() = _rateSell




    fun updateDealState(newState: DealState) {
        _stateDeal.update { newState }
    }


    fun getItemCurrency() {
        CoroutineScope(Dispatchers.IO).launch {
            getFiat()
            getRate()
            getRateSell()
        }
    }

    private suspend fun getRate(currency: String = "USD") {
            val rateResult = repository.getRateBuy(currency)

            if (rateResult != null) {
                if (!rateResult.itemRates.isNullOrEmpty()) {
                    _rateBuy.update { rateResult }
                } else {
                    getRate()
                }
            }

    }

    private suspend fun getRateSell() {
        val rateResult = repository.getRateSell(
            "USD"
        )
        if (rateResult != null) {
            if (!rateResult.itemRates.isNullOrEmpty()) {
                _rateSell.update { rateResult }
            }
        }
    }

    private suspend fun getFiat() {
        val fiatResult = repository.getFiat()
        if (fiatResult != null) {
            updateSelectedFiat(
                fiatResult.data.layoutByCountry[0].countryCode,
                fiatResult.data.layoutByCountry[0]
            )
            _fiatList.update { fiatResult }
        }
    }


    private var _statePaymentOperatorMethod = MutableStateFlow<List<Item.OperatorModel>>(listOf())
    val statePaymentOperatorMethod = _statePaymentOperatorMethod


    private var _stateItemSelectedPayMethod =
        MutableStateFlow<Item.OperatorModel?>(null)
    val stateItemSelectedPayMethod get() = _stateItemSelectedPayMethod


    suspend fun getPaymentOperatorMethodsSell(tonValue: Double) {
        withContext(Dispatchers.IO) {
            val rate = _rateSell.value
            val fiatValue = _selectedFiat.value.layoutByCountry
            val fiatAllList = _fiatList.value
            val listNewModel = mutableListOf<Item.OperatorModel>()


            if(fiatAllList != null && rate != null) {
                if(!rate.itemRates.isNullOrEmpty()) {
                    val listSell = fiatAllList.data.sell[1].items
                    if(listSell != null) {
                        for(i in listSell.indices) {
                            val itemModel = Item.OperatorModel(
                                name = listSell[i].title,
                                priceResult = "${
                                    (tonValue * rate.itemRates[0].rate).toString()
                                        .take(5)
                                } ${rate.itemRates[0].currency} for $tonValue TON",
                                logo = listSell[i].icon_url,
                                stateSelected = false,
                                position = ListCell.Position.FIRST,
                                courseRate = rate.itemRates[0].rate
                            )
                            listNewModel.add(itemModel)
                        }
                    }

                }
                val afterFilteredPos =
                    listNewModel.mapIndexed { index, layoutByOperation ->

                        val position = if (listNewModel.size == 1) {
                            layoutByOperation.stateSelected = true
                            ListCell.Position.SINGLE
                        } else {
                            when (index) {
                                0 -> ListCell.Position.FIRST
                                listNewModel.size - 1 -> ListCell.Position.LAST
                                else -> ListCell.Position.MIDDLE
                            }
                        }
                        layoutByOperation.position = position
                        layoutByOperation
                    }
                _statePaymentOperatorMethod.update { afterFilteredPos }
            }
        }
    }

    suspend fun getPaymentOperatorMethods(tonValue: Double) {
        withContext(Dispatchers.IO) {
            val fiatValue = _selectedFiat.value.layoutByCountry
            val fiatAllResult = _fiatList.value
            if (fiatValue != null && fiatAllResult != null) {
                val rateResult =
                    repository.getRateBuy(fiatValue.currency)
                if (rateResult != null) {
                    if (!rateResult.itemRates.isNullOrEmpty()) {
                        val methodResult = fiatValue.methods
                        if (methodResult.isNotEmpty()) {

                            collectResultPaymentOperatorMethod(
                                methodResult = methodResult,
                                rateResult = rateResult.itemRates,
                                fiatAllResult = fiatAllResult,
                                tonValue = tonValue
                            )

                        }
                    } else {
                        val rateResult =
                            repository.getRateBuy("USD")
                        val usdResult =
                            fiatAllResult.data.layoutByCountry.find { it.countryCode == "US" }
                        if (rateResult != null) {
                            if (!rateResult.itemRates.isNullOrEmpty() && usdResult != null) {
                                updateSelectedFiat(null, usdResult)
                                val methodResult = usdResult.methods
                                if (methodResult.isNotEmpty()) {
                                    collectResultPaymentOperatorMethod(
                                        methodResult = methodResult,
                                        rateResult = rateResult.itemRates,
                                        fiatAllResult = fiatAllResult,
                                        tonValue = tonValue
                                    )
                                }
                            }
                        }
                    }

                }
            }
        }
    }


    private fun collectResultPaymentOperatorMethod(
        methodResult: List<String>,
        rateResult: List<ItemRates>,
        fiatAllResult: FiatModel,
        tonValue: Double
    ) {
        val listNewModel = mutableListOf<Item.OperatorModel>()
        for (i in methodResult.indices) {
            for (m in rateResult.indices) {

                if (methodResult[i] == rateResult[m].id) {
                    val itemsFiatResult = fiatAllResult.data.buy[0].items

                    if(itemsFiatResult != null) {
                        for (s in itemsFiatResult.indices) {
                            if (methodResult[i] == itemsFiatResult[s].id) {
                                val newItemModel = Item.OperatorModel(
                                    name = rateResult[m].name,
                                    priceResult = "${
                                        (tonValue * rateResult[m].rate).toString()
                                            .take(5)
                                    } ${rateResult[0].currency} for $tonValue TON",
                                    logo = itemsFiatResult[s].icon_url,
                                    stateSelected = false,
                                    position = ListCell.Position.FIRST,
                                    courseRate = rateResult[m].rate
                                )
                                listNewModel.add(newItemModel)
                            }
                        }

                    }
                }
            }
        }

        val afterFilteredPos =
            listNewModel.mapIndexed { index, layoutByOperation ->

                val position = if (listNewModel.size == 1) {
                    layoutByOperation.stateSelected = true
                    ListCell.Position.SINGLE
                } else {
                    when (index) {
                        0 -> ListCell.Position.FIRST
                        listNewModel.size - 1 -> ListCell.Position.LAST
                        else -> ListCell.Position.MIDDLE
                    }
                }
                layoutByOperation.position = position
                layoutByOperation
            }
        _statePaymentOperatorMethod.update { afterFilteredPos }
    }

    fun updateSelectedFiat(visibleCurrency: String?, newFiat: LayoutByCountry) {
        _selectedFiat.update {
            SelectedFiatModel(
                visibleCurrency = visibleCurrency ?: _selectedFiat.value.visibleCurrency,
                layoutByCountry = newFiat
            )
        }
        CoroutineScope(Dispatchers.IO).launch {
            getRate(newFiat.currency)
        }
    }

    fun updateSelectedPayMethod(model: Item.OperatorModel) {
        _stateItemSelectedPayMethod.update { model }
    }

    private var _fiatPayMethodItem =
        MutableStateFlow<com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel.Item?>(null)
    val fiatPayMethodItem get() = _fiatPayMethodItem

    fun searchFiatPayMethod(): com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel.Item? {
        val selectedItem = _stateItemSelectedPayMethod.value
        val fiatList = _fiatList.value
        if (selectedItem != null && fiatList != null) {
            val dataForFilter = if (stateDeal.value == DealState.BUY) fiatList.data.buy[0].items else fiatList.data.sell[1].items
            val list = dataForFilter?.find {
                it.title.equals(
                    selectedItem.name,
                    ignoreCase = true
                )
            }
            _fiatPayMethodItem.update { list }
            return list
        } else {
            return null
        }
    }

    init {
        getItemCurrency()
    }
}
