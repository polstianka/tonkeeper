package com.tonapps.tonkeeper.fragment.swap.currency

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.tonapps.tonkeeper.fragment.swap.currency.list.SwapDetailsItem
import com.tonapps.tonkeeper.fragment.swap.model.TokenInfo
import com.tonapps.tonkeeper.fragment.swap.pager.PagerScreen
import com.tonapps.tonkeeper.fragment.swap.token.TokenPickerScreen
import com.tonapps.tonkeeper.fragment.swap.view.SendReceiveView
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.navigation.Navigation.Companion.navigation

class CurrencyScreen : PagerScreen(R.layout.fragment_swap_currency) {

    companion object {
        private const val TOKEN_PICKER_REQUEST_KEY = "token_picker_request"

        fun newInstance() = CurrencyScreen()
    }

    private val feature: CurrencyScreenFeature by viewModel()

    private lateinit var sendReceiveView: SendReceiveView
    private lateinit var actionButtonView: Button
    private lateinit var loadingView: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sendReceiveView = view.findViewById(R.id.send_receive)
        actionButtonView = view.findViewById(R.id.action_button)
        loadingView = view.findViewById(R.id.loading)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                feature.uiState.collect { state -> newUiState(state) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                swapFeature.sendToken.collect { token -> feature.sendTokenChanged(token) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                swapFeature.receiveToken.collect { token -> feature.receiveTokenChanged(token) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                feature.uiEffect.collect(::newEffect)
            }
        }

        navigation?.setFragmentResultListener(TOKEN_PICKER_REQUEST_KEY) { bundle ->
            bundle.getParcelable<TokenInfo>(TokenPickerScreen.RESULT_KEY)?.let { token ->
                feature.onTokenPick(token)
            }
        }

        sendReceiveView.setOnSendTokenClick {
            feature.sendTokenPick()
        }
        sendReceiveView.setOnReceiveTokenClick {
            feature.receiveTokenPick()
        }

        sendReceiveView.setSendOnAmountChangeListener(feature::setSendValue)
        sendReceiveView.setReceiveOnAmountChangeListener(feature::setReceiveValue)

        sendReceiveView.swapButtonClick = feature::onSwapButtonClick

        sendReceiveView.setSendOnBalanceActionClick(feature::balanceActionClick)
        sendReceiveView.onSwapDetailsItemClick = { item ->
            when (item) {
                is SwapDetailsItem.Header -> feature.toggleDetailsItemsVisibility()
                is SwapDetailsItem.Cell -> item.additionalInfo?.let {
                    swapFeature.showMessage(
                        getString(it)
                    )
                }

                else -> Unit
            }
        }

        actionButtonView.setOnClickListener {
            sendReceiveView.hideKeyboard()
            feature.onButtonClick()
        }
    }

    private fun newUiState(state: CurrencyScreenState) {
        swapFeature.updateResultState(state)
        sendReceiveView.sendToken = state.sendInfo.token

        sendReceiveView.receiveToken = state.receiveInfo.token
        sendReceiveView.details = state.details.items
        sendReceiveView.expandedDetails = state.details.expanded
        setButtonState(state.buttonState)
    }

    private fun setButtonState(buttonState: CurrencyScreenState.ButtonState) {
        when (buttonState) {
            CurrencyScreenState.ButtonState.ENTER_AMOUNT -> {
                actionButtonView.setBackgroundResource(uikit.R.drawable.bg_button_secondary)
                actionButtonView.setText(Localization.enter_amount)
                loadingView.visibility = View.GONE
            }

            CurrencyScreenState.ButtonState.CHOOSE_TOKEN -> {
                actionButtonView.setBackgroundResource(uikit.R.drawable.bg_button_secondary)
                actionButtonView.setText(Localization.choose_token)
                loadingView.visibility = View.GONE
            }

            CurrencyScreenState.ButtonState.LOADING -> {
                actionButtonView.setBackgroundResource(uikit.R.drawable.bg_button_secondary)
                actionButtonView.setText("")
                loadingView.visibility = View.VISIBLE
            }

            CurrencyScreenState.ButtonState.CONTINUE -> {
                actionButtonView.setBackgroundResource(uikit.R.drawable.bg_button_primary)
                actionButtonView.setText(Localization.continue_action)
                loadingView.visibility = View.GONE
            }
        }
    }

    private fun newEffect(effect: CurrencyScreenEffect) {
        when (effect) {
            is CurrencyScreenEffect.NavigateToConfirm -> {
                swapFeature.nextPage()
            }

            is CurrencyScreenEffect.OpenTokenPicker -> {
                navigation?.add(
                    TokenPickerScreen.newInstance(
                        tokens = swapFeature.tokens,
                        selected = effect.selected,
                        except = effect.except,
                        request = TOKEN_PICKER_REQUEST_KEY
                    )
                )
            }

            is CurrencyScreenEffect.SetSendAmount -> {
                sendReceiveView.setSendValue(effect.amount.toString())
            }

            is CurrencyScreenEffect.SetReceiveAmount -> {
                sendReceiveView.setReceiveValue(effect.amount.toString())
            }

            is CurrencyScreenEffect.UpdateAmounts -> {
                sendReceiveView.setSendValue(effect.sendAmount.toString())
                sendReceiveView.setReceiveValue(effect.receiveAmount.toString())
            }

            is CurrencyScreenEffect.TakeFocus -> {
                sendReceiveView.takeSendFocus()
            }

        }
    }
}