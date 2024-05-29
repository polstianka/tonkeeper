package com.tonapps.tonkeeper.fragment.swap

import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeper.extensions.getSwapRateColor
import com.tonapps.tonkeeper.fragment.swap.settings.SwapSettingsScreen
import com.tonapps.tonkeeper.fragment.swap.tokens.TokensSelectorScreen
import com.tonapps.tonkeeper.fragment.swap.view.SwapInfoLayoutView
import com.tonapps.tonkeeper.fragment.swap.view.SwapInfoRowView
import com.tonapps.tonkeeper.fragment.swap.view.SwapTokenInputLayoutView
import com.tonapps.tonkeeper.ui.component.BlurredScrollView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundTransparentColor
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.drawable.InputDrawable
import uikit.drawable.TopDividerDrawable
import uikit.extensions.bottomScrolled
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.extensions.hideKeyboard
import uikit.extensions.topScrolled
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FooterViewEmpty
import uikit.widget.HeaderView
import uikit.widget.LoaderView

class SwapAmountScreen : BaseFragment(R.layout.fragment_swap_amount), BaseFragment.BottomSheet {
    private val swapScreenViewModel: SwapScreenViewModel by viewModel()

    private lateinit var headerView: HeaderView
    private lateinit var listView: BlurredScrollView
    private lateinit var footerView: FooterViewEmpty

    private lateinit var nextButton: Button
    private lateinit var nextButtonLoading: LoaderView

    private lateinit var inputAmountContainerDrawable1: InputDrawable
    private lateinit var inputAmountContainerDrawable2: InputDrawable
    private lateinit var tokenInputLayout1: SwapTokenInputLayoutView
    private lateinit var tokenInputLayout2: SwapTokenInputLayoutView

    private lateinit var swapInfoLayoutContainerView: LinearLayoutCompat
    private lateinit var swapInfoLayoutView: SwapInfoLayoutView
    private lateinit var swapInfoRateView: SwapInfoRowView
    private lateinit var swapInfoRateViewLoading: LoaderView

    private lateinit var swapTokensButton: AppCompatImageView

    private var ignoreTextChanged: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerView = view.findViewById(R.id.header)
        headerView.contentMatchParent()
        headerView.setColor(requireContext().backgroundTransparentColor)
        headerView.doOnActionClick = {
            getCurrentFocus()?.hideKeyboard()
            finish()
        }
        headerView.doOnCloseClick = {
            getCurrentFocus()?.hideKeyboard()
            navigation?.add(SwapSettingsScreen.newInstance())
        }


        listView = view.findViewById(R.id.list)
        listView.blurredPaddingTop = 64.dp
        listView.blurredPaddingBottom = 88.dp

        footerView = view.findViewById(R.id.footer)
        footerView.setColor(requireContext().backgroundTransparentColor)



        nextButton = view.findViewById(R.id.next)


        nextButtonLoading = view.findViewById(R.id.next_loader)
        nextButtonLoading.setSize(3f.dp)

        inputAmountContainerDrawable1 = InputDrawable(view.context)
        inputAmountContainerDrawable2 = InputDrawable(view.context)
        view.findViewById<View>(R.id.token_container_2_wrapper).background =
            inputAmountContainerDrawable2

        tokenInputLayout1 = view.findViewById(R.id.token_container_1)
        tokenInputLayout1.background = inputAmountContainerDrawable1
        tokenInputLayout2 = view.findViewById(R.id.token_container_2)

        tokenInputLayout2.maxView.isVisible = false
        tokenInputLayout1.chooseTokenView.setOnClickListener {
            getCurrentFocus()?.hideKeyboard()
            navigation?.add(
                TokensSelectorScreen.newInstance(
                    swapScreenViewModel::setToken1,
                    swapScreenViewModel.getToken2()
                )
            )
        }

        tokenInputLayout2.chooseTokenView.setOnClickListener {
            getCurrentFocus()?.hideKeyboard()
            navigation?.add(
                TokensSelectorScreen.newInstance(
                    swapScreenViewModel::setToken2,
                    swapScreenViewModel.getToken1()
                )
            )
        }

        tokenInputLayout1.amountInput.doOnTextChanged { text, _, _, _ ->
            if (!ignoreTextChanged) swapScreenViewModel.onAmountEnter1(
                text.toString()
            )
        }
        tokenInputLayout2.amountInput.doOnTextChanged { text, _, _, _ ->
            if (!ignoreTextChanged) swapScreenViewModel.onAmountEnter2(
                text.toString()
            )
        }

        swapInfoLayoutContainerView = view.findViewById(R.id.swap_info_layout_container)

        swapInfoLayoutView = view.findViewById(R.id.swap_info_layout)
        swapInfoLayoutView.background = TopDividerDrawable(view.context)

        swapInfoRateView = view.findViewById(R.id.swap_info_rate)
        swapInfoRateView.background = TopDividerDrawable(view.context)

        swapInfoRateViewLoading = view.findViewById(R.id.swap_info_rate_loader)

        swapTokensButton = view.findViewById(R.id.swap_tokens)
        swapTokensButton.setOnClickListener { swapScreenViewModel.swapTokens() }


        collectFlow(swapScreenViewModel.stateFlow) { state ->
            tokenInputLayout1.chooseTokenView.setToken(state.token1?.symbol, state.token1?.imageUri)
            tokenInputLayout2.chooseTokenView.setToken(state.token2?.symbol, state.token2?.imageUri)

            tokenInputLayout1.balanceView.text = state.balanceToken1
            tokenInputLayout2.balanceView.text = state.balanceToken2

            tokenInputLayout1.amountInput.isEnabled = state.token1 != null
            tokenInputLayout2.amountInput.isEnabled = state.token2 != null

            tokenInputLayout1.maxView.isVisible = state.accountToken1 != null
            tokenInputLayout1.maxView.setOnClickListener {
                swapScreenViewModel.setMaxValue()
            }

            ignoreTextChanged = true
            if (!state.reverse) {
                if (tokenInputLayout1.amountInput.text.toString() != state.amountInput.input) {
                    tokenInputLayout1.amountInput.text =
                        Editable.Factory.getInstance().newEditable(state.amountInput.input)
                }
            } else {
                if (tokenInputLayout2.amountInput.text.toString() != state.amountInput.input) {
                    tokenInputLayout2.amountInput.text =
                        Editable.Factory.getInstance().newEditable(state.amountInput.input)
                }
            }
            ignoreTextChanged = false
        }

        collectFlow(swapScreenViewModel.statusFlow) { status ->
            nextButton.text = when (status.buttonState) {
                SwapScreenViewModel.ButtonState.InsufficientBalanceTON -> getString(Localization.insufficient_balance_ton)
                SwapScreenViewModel.ButtonState.InsufficientBalance -> getString(Localization.insufficient_balance)
                SwapScreenViewModel.ButtonState.WaitAmount -> getString(Localization.swap_enter_amount)
                SwapScreenViewModel.ButtonState.WaitToken -> getString(Localization.swap_choose_token)
                SwapScreenViewModel.ButtonState.SimulationError -> getString(Localization.swap_simulate_error)
                SwapScreenViewModel.ButtonState.HighPriceImpact -> getString(Localization.swap_high_price_impact)
                SwapScreenViewModel.ButtonState.Error -> getString(Localization.continue_action)
                SwapScreenViewModel.ButtonState.Ready -> getString(Localization.continue_action)
                SwapScreenViewModel.ButtonState.Loading -> null
            }

            swapInfoRateViewLoading.isVisible =
                status.buttonState == SwapScreenViewModel.ButtonState.Loading || status.simulateLoading
            nextButtonLoading.isVisible =
                status.buttonState == SwapScreenViewModel.ButtonState.Loading
            nextButton.isEnabled = status.buttonState == SwapScreenViewModel.ButtonState.Ready
            inputAmountContainerDrawable1.error = status.tokenError1
            inputAmountContainerDrawable2.error = status.tokenError2
        }

        collectFlow(listView.topScrolled, headerView::setDivider)
        collectFlow(listView.bottomScrolled, footerView::setDivider)

        combine(
            swapScreenViewModel.stateFlow,
            swapScreenViewModel.simulatedSwapFlow
        ) { state, swap ->
            swapInfoRateView.setTitle(swap?.result?.rateFmt)
            swapInfoRateView.setTitleColorInt(
                requireContext().getSwapRateColor(
                    swap?.result?.priceImpact ?: 0f
                )
            )

            swapInfoLayoutContainerView.isVisible = swap?.result != null
            swap?.result?.let {
                swapInfoLayoutView.set(it)
            }

            ignoreTextChanged = true
            if (!state.reverse) {
                swap?.result?.let { res ->
                    state.token2?.let {
                        tokenInputLayout2.amountInput.text = Editable.Factory.getInstance()
                            .newEditable(res.tokenToReceive.units.toString(res.tokenToReceive.token.decimals))
                    }
                } ?: run {
                    tokenInputLayout2.amountInput.text?.clear()
                }
            } else {
                swap?.result?.let { res ->
                    state.token1?.let {
                        tokenInputLayout1.amountInput.text = Editable.Factory.getInstance()
                            .newEditable(res.tokenToSend.units.toString(res.tokenToSend.token.decimals))
                    }
                } ?: run {
                    tokenInputLayout1.amountInput.text?.clear()
                }
            }
            ignoreTextChanged = false

            nextButton.setOnClickListener {
                getCurrentFocus()?.hideKeyboard()
                swapScreenViewModel.prepareConfirmPage()
                navigation?.add(SwapConfirmScreen.newInstance(this))
            }


        }.launchIn(lifecycleScope)
    }

    override fun getViewForNestedScrolling(): View {
        return listView
    }

    override fun onDragging() {
        super.onDragging()
        getCurrentFocus()?.hideKeyboard()
    }

    companion object {
        fun newInstance() = SwapAmountScreen()
    }
}