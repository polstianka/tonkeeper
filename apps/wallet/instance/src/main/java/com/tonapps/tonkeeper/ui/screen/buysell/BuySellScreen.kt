package com.tonapps.tonkeeper.ui.screen.buysell

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.api.buysell.TradeType
import com.tonapps.tonkeeper.dialog.fiat.FiatDialog
import com.tonapps.tonkeeper.fragment.country.CountryScreen
import com.tonapps.tonkeeper.ui.screen.buysell.pager.BuySellScreenAdapter
import com.tonapps.tonkeeper.ui.screen.buysell.pager.PagerScreen
import com.tonapps.tonkeeper.ui.screen.picker.list.WalletPickerAdapter
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.wallet.data.settings.SettingsRepository
import org.koin.android.ext.android.inject
import uikit.base.BaseFragment
import uikit.extensions.doKeyboardAnimation
import uikit.extensions.hideKeyboard
import uikit.mvi.UiScreen
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView

class BuySellScreen :
    UiScreen<BuySellScreenState, BuySellScreenEffect, BuySellScreenFeature>(R.layout.fragment_buysell),
    BaseFragment.BottomSheet {

    companion object {

        const val BS_DIALOG_REQUEST = "buy_sell_dialog_request"
        fun newInstance(): BuySellScreen {
            val fragment = BuySellScreen()
            return fragment
        }
    }

    override val feature: BuySellScreenFeature by viewModels()

    private lateinit var buy: AppCompatTextView
    private lateinit var sell: AppCompatTextView
    private lateinit var lang: AppCompatTextView

    private lateinit var pageAdapter: BuySellScreenAdapter
    private var fromPage = 0

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            feature.setCurrentPage(position)
            if (position == 0) {
                headerView.setDefault()
                headerView.setIcon(0)

                buy.animate().cancel()
                buy.alpha = 0f
                buy.animate().alpha(1f).setDuration(200).start()

                sell.animate().cancel()
                sell.alpha = 0f
                sell.animate().alpha(1f).setDuration(200).start()

                lang.animate().cancel()
                lang.alpha = 0f
                lang.animate().alpha(1f).setDuration(200).start()

                buy.isClickable = true
                sell.isClickable = true
                lang.isClickable = true
            } else {
                buy.animate().cancel()
                buy.animate().alpha(0f).setDuration(200).start()

                sell.animate().cancel()
                sell.animate().alpha(0f).setDuration(200).start()

                lang.animate().cancel()
                lang.animate().alpha(0f).setDuration(200).start()

                if (position != BuySellScreenAdapter.POSITION_CURRENCY) {
                    headerView.setIcon(UIKitIcon.ic_chevron_left_16)
                } else {
                    headerView.setIcon(0)
                }

                buy.isClickable = false
                sell.isClickable = false
                lang.isClickable = false
            }
            for (i in 0 until pageAdapter.itemCount) {
                val fragment = pageAdapter.findFragmentByPosition(i) as? PagerScreen<*, *, *>
                fragment?.visible = i == position
                fragment?.view?.alpha = if (i == position || i == fromPage) 1f else 0f
            }
        }
    }

    private lateinit var headerView: HeaderView
    private lateinit var pagerView: ViewPager2
    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageAdapter = BuySellScreenAdapter(this)

        navigation?.setFragmentResultListener(BS_DIALOG_REQUEST) { bundle ->
            lang.text = settingsRepository.country
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.contentMatchParent()
        headerView.doOnActionClick = {
            if (pagerView.currentItem == BuySellScreenAdapter.POSITION_CURRENCY) {
                feature.setCurrentPage(BuySellScreenAdapter.POSITION_OPERATOR)
            } else {
                finish()
            }
        }
        headerView.doOnCloseClick = {
            when (pagerView.currentItem) {
                BuySellScreenAdapter.POSITION_CONFIRM -> feature.setCurrentPage(BuySellScreenAdapter.POSITION_OPERATOR)
                BuySellScreenAdapter.POSITION_OPERATOR -> feature.setCurrentPage(BuySellScreenAdapter.POSITION_AMOUNT)
            }
        }

        buy = view.findViewById(R.id.buy)
        sell = view.findViewById(R.id.sell)
        lang = view.findViewById(R.id.lang)

        lang.text = settingsRepository.country
        lang.setOnClickListener {
            navigation?.add(CountryScreen.newInstance(BS_DIALOG_REQUEST))
        }

        buy.setOnClickListener {
            buy.isSelected = true
            sell.isSelected = false
            pageAdapter.amountScreen?.setTradeType(TradeType.BUY)
        }

        sell.setOnClickListener {
            buy.isSelected = false
            sell.isSelected = true
            pageAdapter.amountScreen?.setTradeType(TradeType.SELL)
        }

        buy.isSelected = true

        pagerView = view.findViewById(R.id.pager)
        pagerView.offscreenPageLimit = 3
        pagerView.isUserInputEnabled = false
        pagerView.adapter = pageAdapter
        pagerView.registerOnPageChangeCallback(pageChangeCallback)

        view.doKeyboardAnimation { offset, progress, _ ->
            if (pageAdapter.confirmScreen?.isVisibleForUser() == true)
                pageAdapter.confirmScreen?.onKeyboardAppear(progress)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pagerView.unregisterOnPageChangeCallback(pageChangeCallback)
    }

    override fun onEndShowingAnimation() {
        super.onEndShowingAnimation()
        feature.readyView()
    }

    override fun onDragging() {
        super.onDragging()
        getCurrentFocus()?.hideKeyboard()
    }

    override fun newUiState(state: BuySellScreenState) {
        fromPage = pagerView.currentItem
        pagerView.currentItem = state.currentPage
        headerView.title = state.headerTitle
        headerView.setSubtitle(state.headerSubtitle)
        if (state.headerVisible) {
            headerView.showText()
        } else {
            headerView.hideText()
        }
    }

    override fun newUiEffect(effect: BuySellScreenEffect) {
        super.newUiEffect(effect)

        if (effect is BuySellScreenEffect.Finish) {
            finish()
        }
    }
}