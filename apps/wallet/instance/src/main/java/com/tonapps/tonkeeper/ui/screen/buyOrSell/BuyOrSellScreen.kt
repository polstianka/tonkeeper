package com.tonapps.tonkeeper.ui.screen.buyOrSell

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope

import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.currencylist.CurrencyListScreen
import com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.operator.OperatorScreen
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.BuyOrSellViewModel
import com.tonapps.tonkeeper.ui.screen.buyOrSell.view.HeaderBuyOrSellView
import com.tonapps.tonkeeperx.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uikit.base.BaseFragment
import uikit.navigation.Navigation.Companion.navigation

class BuyOrSellScreen : BaseFragment(R.layout.fragment_buy_or_sell), BaseFragment.BottomSheet {

    private lateinit var headerButOrSell: HeaderBuyOrSellView
    private lateinit var viewPager: ViewPager2

    private val viewModel: BuyOrSellViewModel by activityViewModels()
    private val args: BuyOrSellArgs by lazy { BuyOrSellArgs(requireArguments()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Init views

        headerButOrSell = view.findViewById(R.id.headerButOrSell)
        viewPager = view.findViewById(R.id.viewPager)
        val adapter = ViewPagerAdapter(requireContext() as FragmentActivity) {
            navigation?.add(OperatorScreen.newInstance(
             it,
                args.address
            ))
        }
        viewPager.adapter = adapter
        headerButOrSell.setupWithViewPager(viewPager)
        headerButOrSell.doOnCloseClick = {
            finish()
        }

        lifecycleScope.launch {
            val stateSelectedFiat = launch {
                viewModel.selectedFiat.collectLatest {
                    if (it != null) {
                        headerButOrSell.setSelectedTypeCurrency(it.visibleCurrency)
                    }
                }
            }

            stateSelectedFiat.join()
        }

        headerButOrSell.doOnSelectLanguageClick = {
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

    override fun onResume() {
        super.onResume()
        viewModel.getItemCurrency()
    }

    companion object {

        fun newInstance(
            address: String,
        ): BuyOrSellScreen {
            val fragment = BuyOrSellScreen()
            fragment.arguments = BuyOrSellArgs(address = address).toBundle()
            return fragment
        }
    }
}