package com.tonapps.tonkeeper.ui.screen.buysell.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.ui.screen.buysell.currency.BuySellCurrencyScreen
import com.tonapps.tonkeeper.ui.screen.buysell.operator.OperatorScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.focusWithKeyboard
import uikit.extensions.hideKeyboard
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView
import uikit.widget.ProgressButton

class BuySellScreen : BaseFragment(R.layout.fragment_buy_sell), BaseFragment.BottomSheet,
    BuySellListener {

    private val viewModel: BuySellViewModel by viewModel()

    private lateinit var headerView: HeaderView
    private lateinit var rateTetx: AppCompatTextView
    private lateinit var minAmountText: AppCompatTextView
    private lateinit var valueView: AmountInput
    private lateinit var continueButton: ProgressButton
    private lateinit var paymentMethodsContainer: LinearLayoutCompat

    private var selectedRadioButton: RadioButton? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        handleViews()
        handleViewModel()

    }

    private fun initializeViews(view: View) {
        headerView = view.findViewById(R.id.header)
        rateTetx = view.findViewById(R.id.rate)
        minAmountText = view.findViewById(R.id.min_amount)
        valueView = view.findViewById(R.id.value)
        continueButton = view.findViewById(R.id.continue_progress_button)
        paymentMethodsContainer = view.findViewById(R.id.payment_methods_container)
    }


    private fun handleViews() {

        headerView.doOnActionClick = { finish() }
        headerView.doOnCloseClick = {
            // Because APIs are not completed yet, it currently makes more sense to select the currency
            // instead of selecting, the country.
            navigation?.add(BuySellCurrencyScreen.newInstance())
            // navigation?.add(BuySellCountryScreen.newInstance(BUY_SELL_REQUEST))
        }

        minAmountText.setText(String.format(getString(Localization.min_amount), "50 TON"))

        valueView.doAfterValueChanged = viewModel::setValue
        valueView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                if (viewModel.screenStateFlow.value.continueState == BuySellScreenState.ContinueState.NEXT) {
                    next()
                } else {
                    valueView.focusWithKeyboard()
                }
            }
            true
        }

        handlePaymentMethods()

    }

    fun handleViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.screenStateFlow.collect { screenState ->
                newUiState(screenState)
            }
        }
    }

    private fun handlePaymentMethods() {
        val paymentMethods = listOf(
            PaymentMethod("Credit Card", R.drawable.ic_card_master),
            PaymentMethod("Credit Card · RUB", R.drawable.ic_card_mnp),
        )

        val inflater = LayoutInflater.from(requireContext())

        paymentMethods.forEachIndexed { index, paymentMethod ->
            val itemView =
                inflater.inflate(R.layout.view_pay_method, paymentMethodsContainer, false)

            val itemContainer: LinearLayoutCompat = itemView.findViewById(R.id.payment_item)
            val radioButton: RadioButton = itemView.findViewById(R.id.radio_button)
            val paymentMethodName: TextView = itemView.findViewById(R.id.payment_method_name)
            val paymentMethodIcon: ImageView = itemView.findViewById(R.id.payment_method_icon)
            val line: View = itemView.findViewById(R.id.line)

            paymentMethodName.text = paymentMethod.name
            paymentMethodIcon.setImageResource(paymentMethod.iconResId)
            val backgroundDrawable = when (index) {
                0 -> uikit.R.drawable.bg_content_top_radius
                paymentMethods.size - 1 -> uikit.R.drawable.bg_content_bottom_radius
                else -> uikit.R.drawable.bg_content
            }
            itemContainer.setBackgroundResource(backgroundDrawable)
            if (index == paymentMethods.size - 1) line.visibility = View.GONE

            itemView.setOnClickListener {
                selectedRadioButton?.isChecked = false
                radioButton.isChecked = true
                selectedRadioButton = radioButton

                viewModel.setPaymentMethodSelection()
            }

            paymentMethodsContainer.addView(itemView)

        }

    }

    private fun newUiState(screenState: BuySellScreenState) {
        // rateTetx.text = "0 ${screenState.currencyCode}"
        rateTetx.text = screenState.rate


        continueButton.onClick = { _, _ ->
            next()
        }
        continueButton.apply {
            when (screenState.continueState) {
                is BuySellScreenState.ContinueState.DISABLE -> {
                    isEnabled = false
                    setText(getString(Localization.continue_action))
                }

                is BuySellScreenState.ContinueState.ENTER_AMOUNT -> {
                    isEnabled = false
                    setText(getString(Localization.enter_amount))
                }

                is BuySellScreenState.ContinueState.SELECT_PAYMENT -> {
                    isEnabled = false
                    setText(getString(Localization.select_payment_method))
                }

                is BuySellScreenState.ContinueState.NEXT -> {
                    isEnabled = true
                    setText(getString(Localization.continue_action))
                }
            }
        }

    }

    private fun next() {
        OperatorScreen.newInstance(inputAmount = viewModel.inputValue).also { operatorScreen ->
            postDelayed(500) { getCurrentFocus()?.hideKeyboard() }
            operatorScreen.setBuySellListener(this)
            navigation?.add(operatorScreen)
        }
    }

    override fun onDismiss() {
        finish()
    }


    companion object {

        const val BUY_SELL_REQUEST = "buy_sell_request"

        fun newInstance(
        ): BuySellScreen {
            val fragment = BuySellScreen()
            return fragment
        }
    }

    data class PaymentMethod(
        val name: String,

        @DrawableRes
        val iconResId: Int
    )

}