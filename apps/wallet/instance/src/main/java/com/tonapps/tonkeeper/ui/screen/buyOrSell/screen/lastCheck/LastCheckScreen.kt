package com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.lastCheck

import android.annotation.SuppressLint
import android.os.Bundle
import android.telephony.PhoneNumberUtils.formatNumber
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.currencylist.CurrencyListArgs
import com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.currencylist.CurrencyListScreen
import com.tonapps.tonkeeper.ui.screen.buyOrSell.screen.viewing.ViewingScreen
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.BuyOrSellViewModel
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.DealState
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel.Item
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.fiatModel.LayoutByCountry
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.formatNumber
import com.tonapps.tonkeeper.ui.screen.buyOrSell.utils.model.replaceFlagsFromUrl
import com.tonapps.tonkeeper.ui.screen.buyOrSell.view.InputViewAmount
import com.tonapps.tonkeeper.ui.screen.swap.view.progressButton.ProgressButton
import com.tonapps.tonkeeperx.R
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.internal.addHeaderLenient
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.w3c.dom.Text
import uikit.base.BaseFragment
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FrescoView
import uikit.widget.HeaderView
import uikit.widget.InputView

class LastCheckScreen : BaseFragment(R.layout.fragment_last_check), BaseFragment.BottomSheet {

    private val viewModel: BuyOrSellViewModel by activityViewModel()

    private val args: LastCheckArgs by lazy { LastCheckArgs(requireArguments()) }
    private var isUpdating = false
    private lateinit var imgCurrency: FrescoView
    private lateinit var txMarket: TextView
    private lateinit var txSubtitle: TextView
    private lateinit var inputPay: InputViewAmount
    private lateinit var inputGet: InputViewAmount
    private lateinit var resultPrice: TextView
    private lateinit var progressButton: ProgressButton
    private lateinit var txProvider: TextView
    private lateinit var groupInfoBtn: LinearLayout
    private lateinit var headerView: HeaderView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // init views
        imgCurrency = view.findViewById(R.id.imgCurrency)
        txMarket = view.findViewById(R.id.txMarket)
        txSubtitle = view.findViewById(R.id.txSubtitle)
        inputPay = view.findViewById(R.id.inputPay)
        inputGet = view.findViewById(R.id.inputGet)
        resultPrice = view.findViewById(R.id.resultPrice)
        progressButton = view.findViewById(R.id.progressButton)
        txProvider = view.findViewById(R.id.txProvider)
        groupInfoBtn = view.findViewById(R.id.groupInfoBtn)
        headerView = view.findViewById(R.id.header)


        headerView.doOnActionClick = {
            finish()
        }

        progressButton.setUpLoading(false)
        progressButton.updateStateEnabledButton(true)
        progressButton.updateTextInButton("Continue")
        lifecycleScope.launch {
            val fiatPayMethodItem = launch {
                viewModel.fiatPayMethodItem.collectLatest {
                    if (it != null) {
                        setUpViewContent(it)
                    }
                }
            }

            fiatPayMethodItem.join()
        }

    }


    @SuppressLint("SetTextI18n")
    private fun setUpViewContent(item: Item) {
        val urlLoad = replaceFlagsFromUrl(
            template = item.action_button.url,
            countryCode = args.countryNm,
            currencyNm = args.receiveCurrencyNm.uppercase(),
            address = args.address,
            getCurrency = "TON",
            txId = "1"
        )
        imgCurrency.setImageURI(item.icon_url.toUri())
        txMarket.text = item.title
        txSubtitle.text = item.subtitle
        txProvider.text = "Service provided by ${item.title}"

        inputPay.inputType = EditorInfo.TYPE_NUMBER_FLAG_DECIMAL
        inputGet.inputType = EditorInfo.TYPE_NUMBER_FLAG_DECIMAL
        inputPay.doOnIconClick = {
            inputPay.hideKeyboard()
        }
        inputGet.doOnIconClick = {
            inputGet.hideKeyboard()
        }
        inputPay.setTextToTxTypeValue(args.sendCurrencyNm.uppercase())
        inputGet.setTextToTxTypeValue(args.receiveCurrencyNm.uppercase())
        if (viewModel.stateDeal.value == DealState.BUY) {
            inputPay.text = (args.sendAmount * args.courseRate).toString().formatNumber()
            inputGet.text = args.sendAmount.toString().formatNumber()
        } else {
            inputPay.text = args.sendAmount.toString().formatNumber()
            inputGet.text = (args.sendAmount * args.courseRate).toString().formatNumber()
        }

        inputPay.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isUpdating) {
                    isUpdating = true
                    updateGetInput()
                    isUpdating = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        inputGet.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isUpdating) {
                    isUpdating = true
                    updateSendInput()
                    isUpdating = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        resultPrice.text = "${
            args.courseRate.toString().formatNumber()
        } ${args.receiveCurrencyNm.uppercase()} for 1 ${args.sendCurrencyNm}"

        if (!item.info_buttons.isNullOrEmpty()) {
            val firstButton = item.info_buttons[0]
            val secondButton = item.info_buttons[1]
            val viewFirstBtn = groupInfoBtn.findViewById<TextView>(R.id.txInfoBtnOne)
            val viewSecondBtn = groupInfoBtn.findViewById<TextView>(R.id.txInfoBtnTwo)
            groupInfoBtn.isVisible = true
            viewFirstBtn.text = firstButton.title
            viewSecondBtn.text = secondButton.title
            viewFirstBtn.setOnClickListener {
                navigation?.add(
                    ViewingScreen.newInstance(
                        urlLoad = firstButton.url,
                        upBarTitle = firstButton.title
                    )
                )
            }
            viewSecondBtn.setOnClickListener {
                navigation?.add(
                    ViewingScreen.newInstance(
                        urlLoad = urlLoad,
                        upBarTitle = secondButton.title
                    )
                )
            }
        } else {
            groupInfoBtn.isVisible = false
        }


        progressButton.buttonClickListener = {
            Log.d("urlLoad", "urlLoad -$urlLoad")
            navigation?.add(
                ViewingScreen.newInstance(
                    urlLoad = item.action_button.url,
                    upBarTitle = item.title
                )
            )
        }
    }

    fun updateGetInput() {
        val payValue = inputPay.text.toDoubleOrNull()
        if (payValue != null) {
            if (viewModel.stateDeal.value == DealState.BUY) {
                val resultFromCalc = (payValue / args.courseRate)
                if (resultFromCalc >= 50.0) {
                    inputGet.text = resultFromCalc.toString().formatNumber()
                }
            } else {
                val resultFromCalc = (payValue * args.courseRate)
                if (payValue >= 50.0) {
                    inputGet.text = resultFromCalc.toString().formatNumber()
                }
            }
        }
    }

    fun updateSendInput() {
        val payValue = inputGet.text.toDoubleOrNull()
        if (payValue != null) {
            if (viewModel.stateDeal.value == DealState.BUY) {
                val resultFromCalc = (payValue * args.courseRate)
                if (payValue >= 50.0) {
                    inputPay.text = resultFromCalc.toString().formatNumber()
                }
            } else {
                val resultFromCalc = (payValue / args.courseRate)
                if (resultFromCalc >= 50.0) {
                    inputPay.text = resultFromCalc.toString().formatNumber()
                }
            }
        }
    }


    companion object {
        fun newInstance(
            courseRate: Double,
            sendAmount: Double,
            sendCurrencyNm: String,
            receiveCurrencyNm: String,
            countryNm: String,
            address: String
        ): LastCheckScreen {
            val fragment = LastCheckScreen()
            fragment.arguments =
                LastCheckArgs(
                    courseRate = courseRate,
                    sendAmount = sendAmount,
                    sendCurrencyNm = sendCurrencyNm,
                    receiveCurrencyNm = receiveCurrencyNm,
                    countryNm = countryNm,
                    address = address
                ).toBundle()
            return fragment
        }
    }
}