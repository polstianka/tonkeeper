package com.tonapps.tonkeeper.dialog.trade.operator.confirmation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.dialog.trade.TradeViewModel
import com.tonapps.tonkeeper.dialog.trade.operator.OperatorItem
import com.tonapps.tonkeeper.fragment.fiat.web.FiatWebFragment
import com.tonapps.tonkeeperx.R
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.doKeyboardAnimation
import uikit.extensions.hideKeyboard
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FrescoView
import uikit.widget.HeaderView

class ConfirmationTradeScreen :
    BaseFragment(R.layout.fragment_confirmation),
    BaseFragment.BottomSheet {
    companion object {
        private const val OPERATOR_KEY = "operator"

        fun newInstance(operatorItem: OperatorItem): ConfirmationTradeScreen {
            return ConfirmationTradeScreen().apply {
                val bundle = Bundle()
                bundle.putParcelable(OPERATOR_KEY, operatorItem)
                arguments = bundle
            }
        }
    }

    private lateinit var payEdit: EditText
    private lateinit var paymentEditHint: TextView
    private lateinit var receiveEdit: EditText
    private lateinit var getEditHint: TextView
    private lateinit var operatorImage: FrescoView
    private lateinit var titleText: TextView
    private lateinit var subtitleText: TextView
    private lateinit var rateText: TextView
    private lateinit var headerView: HeaderView
    private lateinit var continueButton: Button
    private lateinit var minAmountText: TextView
    private var payTextWatcher: TextWatcher? = null
    private var receiveTextWatcher: TextWatcher? = null
    private val tradeViewModel by activityViewModel<TradeViewModel>()

    private val confirmationViewModel by viewModel<ConfirmationTradeViewModel>()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        payEdit = view.findViewById(R.id.payment_edit)
        // payEdit.requestFocus()
        updatePaymentTextListener()
        paymentEditHint = view.findViewById(R.id.payment_edit_hint)
        receiveEdit = view.findViewById(R.id.get_payment_edit)
        updateReceivePaymentTextListener()
        getEditHint = view.findViewById(R.id.get_payment_edit_hint)
        operatorImage = view.findViewById(R.id.operator_image)
        titleText = view.findViewById(R.id.title_text)
        subtitleText = view.findViewById(R.id.subtitle_text)
        rateText = view.findViewById(R.id.rate_text)
        headerView = view.findViewById(R.id.header)
        continueButton = view.findViewById(R.id.continue_button)
        minAmountText = view.findViewById(R.id.min_amount_text)
        headerView.doOnCloseClick = { finish() }
        headerView.doOnActionClick = { finish() }
        val operatorItem = requireArguments().getParcelable<OperatorItem>(OPERATOR_KEY)
        operatorImage.setImageURI(operatorItem?.iconUrl)
        titleText.text = operatorItem?.title
        subtitleText.text = operatorItem?.subtitle
        rateText.text =
            getString(
                com.tonapps.wallet.localization.R.string.rate_for_one_ton,
                "${
                    CurrencyFormatter.format(
                        "",
                        operatorItem?.rate ?: 0.0,
                    )
                } ${operatorItem?.fiatCurrency}",
            )
        confirmationViewModel.submitOperatorItem(
            operatorItem!!,
            tradeViewModel.isBuyMode.value,
        )
        continueButton.setOnClickListener {
            confirmationViewModel.onContinueClicked()
        }
        view.doKeyboardAnimation { offset, _, _ ->
            continueButton.translationY = -offset.toFloat()
        }
        receiveEdit.post {
            if (tradeViewModel.isBuyMode.value) {
                receiveEdit.setText(
                    CurrencyFormatter.format(
                        "",
                        tradeViewModel.amountFlow.value ?: 0.0,
                    ).toString(),
                )
            } else {
                payEdit.setText(
                    CurrencyFormatter.format("", tradeViewModel.amountFlow.value ?: 0.0).toString(),
                )
            }
        }
        if (tradeViewModel.isBuyMode.value) {
            if (operatorItem.minTonBuyAmount != 0.0) {
                minAmountText.text =
                    getString(
                        com.tonapps.wallet.localization.R.string.min_amount,
                        CurrencyFormatter.format("", operatorItem.minTonBuyAmount).toString(),
                    )
            }
        } else {
            if (operatorItem.minTonSellAmount != 0.0) {
                minAmountText.text =
                    getString(
                        com.tonapps.wallet.localization.R.string.min_amount,
                        CurrencyFormatter.format("", operatorItem.minTonSellAmount).toString(),
                    )
            }
        }
        collectViewModelFlows()
    }

    private fun updatePaymentTextListener() {
        payTextWatcher =
            object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int,
                ) {
                }

                override fun onTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int,
                ) {
                }

                override fun afterTextChanged(text: Editable?) {
                    if (text?.lastOrNull() == '.') return
                    (text.toString().filter { it != ',' }.toDoubleOrNull() ?: 0.0).let {
                        payEdit.removeTextChangedListener(this)
                        receiveEdit.removeTextChangedListener(receiveTextWatcher)
                        confirmationViewModel.onPayChanged(it)
                    }
                }
            }
        payEdit.addTextChangedListener(payTextWatcher)
    }

    private fun updateReceivePaymentTextListener() {
        receiveTextWatcher =
            object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int,
                ) {
                }

                override fun onTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int,
                ) {
                }

                override fun afterTextChanged(text: Editable?) {
                    if (text?.lastOrNull() == '.') return
                    (text.toString().filter { it != ',' }.toDoubleOrNull() ?: 0.0).let {
                        receiveEdit.removeTextChangedListener(this)
                        payEdit.removeTextChangedListener(payTextWatcher)
                        confirmationViewModel.onReceiveChanged(it)
                    }
                }
            }
        receiveEdit.addTextChangedListener(receiveTextWatcher)
    }

    private fun collectViewModelFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                confirmationViewModel
                    .payCurrencyFlow
                    .onEach {
                        paymentEditHint.text = it
                    }.launchIn(this)
                confirmationViewModel
                    .getCurrencyFlow
                    .onEach {
                        getEditHint.text = it
                    }.launchIn(this)
                confirmationViewModel
                    .paymentInfoFlow
                    .onEach {
                        payEdit.setText(CurrencyFormatter.format("", it.pay))
                        receiveEdit.setText(CurrencyFormatter.format("", it.get))
                        if (payEdit.isFocused) {
                            payEdit.setSelection(payEdit.text.length)
                        } else if (receiveEdit.isFocused) {
                            receiveEdit.setSelection(receiveEdit.text.length)
                        }
                        updatePaymentTextListener()
                        updateReceivePaymentTextListener()
                    }.launchIn(this)
                confirmationViewModel
                    .continueFlow
                    .onEach {
                        payEdit.hideKeyboard()
                        receiveEdit.hideKeyboard()
                        navigation?.add(
                            FiatWebFragment.newInstance(
                                url = it.url,
                                pattern = it.pattern,
                            ),
                        )
                    }.launchIn(this)
                confirmationViewModel
                    .getContinueButtonAvailableFlow()
                    .onEach { isAvailable ->
                        continueButton.isEnabled = isAvailable
                    }.launchIn(this)
            }
        }
    }
}
