package com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.currencylist

import android.os.Bundle
import android.util.Log
import android.view.View
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.BuyOrSellViewModel
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel.LayoutByCountry
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.toItem
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.ListCell
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import uikit.base.BaseFragment
import uikit.widget.HeaderView
import uikit.widget.SimpleRecyclerView

class CurrencyListScreen : BaseFragment(R.layout.fragment_currency_list), BaseFragment.BottomSheet {

    private val args: CurrencyListArgs by lazy { CurrencyListArgs(requireArguments()) }
    private lateinit var list: SimpleRecyclerView
    private lateinit var header: HeaderView

    private val viewModel: BuyOrSellViewModel by activityViewModel()
    private val adapter by lazy { CurrencyAdapter(viewModel.selectedFiat.value.layoutByCountry) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list = view.findViewById(R.id.list)
        header = view.findViewById(R.id.header)

        header.doOnActionClick = {
            finish()
        }

        list.adapter = adapter
        val item = convertToItem(args.itemList)
        Log.d("CurrencyListScreen", "itemList = ${args.itemList}")
        adapter.submitList(item)
        adapter.onClickToItem = {
            viewModel.updateSelectedFiat(it.countryCode,it)
            finish()
        }
    }


    private fun convertToItem(list: List<LayoutByCountry>): List<Item.CurrencyList> {
        return list.mapIndexed { index, layoutByCountry ->

            val position = if (list.size == 1) {
                ListCell.Position.SINGLE
            } else {
                when (index) {
                    0 -> ListCell.Position.FIRST
                    list.size - 1 -> ListCell.Position.LAST
                    else -> ListCell.Position.MIDDLE
                }
            }

            layoutByCountry.toItem(position)
        }
    }


    companion object {

        fun newInstance(
            itemList: List<LayoutByCountry>
        ): CurrencyListScreen {
            val fragment = CurrencyListScreen()
            fragment.arguments = CurrencyListArgs(itemList).toBundle()
            return fragment
        }
    }

}