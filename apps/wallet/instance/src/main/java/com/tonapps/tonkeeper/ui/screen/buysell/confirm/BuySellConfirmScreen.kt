package com.tonapps.tonkeeper.ui.screen.buysell.confirm

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.api.fromJSON
import com.tonapps.tonkeeper.api.toJSON
import com.tonapps.tonkeeper.core.fiat.models.FiatItem
import com.tonapps.tonkeeper.fragment.fiat.web.FiatWebFragment
import com.tonapps.tonkeeper.ui.screen.buysell.operator.OperatorListener
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.rates.entity.OperatorBuyRateEntity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView
import uikit.widget.InputView
import uikit.widget.ProgressButton

class BuySellConfirmScreen : BaseFragment(R.layout.fragment_buy_sell_confirm),
    BaseFragment.BottomSheet {

    private val viewModel: BuySellConfirmViewModel by viewModel()

    private val args: BuySellConfirmArgs by lazy { BuySellConfirmArgs(requireArguments()) }

    var operatorListener: OperatorListener? = null

    var amount: Double = 0.0
    var operatorBuyRate: OperatorBuyRateEntity? = null
    lateinit var fiatItem: FiatItem

    lateinit var headerView: HeaderView
    lateinit var icon: SimpleDraweeView
    lateinit var title: AppCompatTextView
    lateinit var subtitle: AppCompatTextView
    lateinit var youPayInput: InputView
    lateinit var youGetInput: InputView
    lateinit var rateText: AppCompatTextView
    lateinit var continueButton: ProgressButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args.also {
            amount = it.amount

            operatorBuyRate = it.operatorBuyRate

            if (it.fiatItem.isNotEmpty())
                fiatItem = fromJSON(it.fiatItem)
            else throw IllegalArgumentException("Fiat item should not be empty!")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        handleViews()
        handleViewModel()

    }

    private fun initializeViews(view: View) {

        headerView = view.findViewById(R.id.header)
        icon = view.findViewById(R.id.icon)
        title = view.findViewById(R.id.title)
        subtitle = view.findViewById(R.id.subtitle)
        youPayInput = view.findViewById(R.id.you_pay_input)
        youPayInput.inputType = EditorInfo.TYPE_NUMBER_FLAG_DECIMAL
        youGetInput = view.findViewById(R.id.you_get_input)
        youGetInput.inputType = EditorInfo.TYPE_NUMBER_FLAG_DECIMAL
        rateText = view.findViewById(R.id.rate)
        continueButton = view.findViewById(R.id.continue_progress_button)

    }

    var lockPay = false
    var lockGet = false

    private fun handleViews() {

        headerView.doOnActionClick = { finish() }
        continueButton.onClick = { _, _ ->
            if (
                youPayInput.text.isNotEmpty() &&
                youGetInput.text.isNotEmpty()
            //&& amount > 0
            ) {
                Log.d("redirect-web", "handleViews: true")
                navigation?.add(
                    FiatWebFragment.newInstance(
                        fiatItem.actionButton.url,
                        fiatItem.successUrlPattern
                    )
                )

                finish()
                operatorListener?.onDismiss()
            } else {
                Log.d("redirect-web", "handleViews: false")
            }

        }

        icon.setImageURI(fiatItem.iconUrl)
        title.text = fiatItem.title
        subtitle.text = fiatItem.subtitle

        youGetInput.text = amount.toString()

        operatorBuyRate?.also { operatorRate ->
            youPayInput.text = (amount * operatorRate.rate).toString()

            val rateNumber = CurrencyFormatter.format(value = operatorRate.rate)
            rateText.text = "${rateNumber} ${operatorRate.currency} for 1 TON"
        }

        youPayInput.doOnTextChange = {
            if (!lockPay) {
                lockGet = true
                operatorBuyRate?.also { operatorRate ->
                    amount = (it.toDoubleOrNull() ?: 0.0) / operatorRate.rate
                    youGetInput.text = amount.toString()
                }
                lockGet = false
            }
        }

        youGetInput.doOnTextChange = {
            if (!lockGet) {
                try {
                    lockPay = true
                    amount = it.toDoubleOrNull() ?: 0.0
                    operatorBuyRate?.also { operatorRate ->
                        youPayInput.text = (amount * operatorRate.rate).toString()
                    }
                    lockPay = false
                } catch (E: NumberFormatException) {

                }
            }
        }

    }

    private fun handleViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.screenStateFlow.collect { screenState ->
                newUiState(screenState)
            }
        }
    }

    private fun newUiState(screenState: BuySellConfirmScreenState) = screenState.apply {

    }

    fun setOpListener(listener: OperatorListener) {
        this.operatorListener = listener
    }

    companion object {

        fun newInstance(
            inputAmount: Double,
            operatorBuyRate: OperatorBuyRateEntity?,
            fiatItem: FiatItem
        ): BuySellConfirmScreen {
            val fragment = BuySellConfirmScreen()

            // val fiatItemJsonString = fiatItem.toJSON().toString()
            val fiatItemJsonString = toJSON(fiatItem)
            fragment.arguments =
                BuySellConfirmArgs(inputAmount, operatorBuyRate, fiatItemJsonString).toBundle()

            return fragment
        }
    }


}