package com.tonapps.tonkeeper.fragment.swap

import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.fragment.swap.view.SwapInfoLayoutView
import com.tonapps.tonkeeper.fragment.swap.view.SwapTokenInputLayoutView
import com.tonapps.tonkeeper.helper.flow.TransactionSender
import com.tonapps.tonkeeper.ui.component.BlurredScrollView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundTransparentColor
import kotlinx.coroutines.flow.filterNotNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.drawable.TopDividerDrawable
import uikit.extensions.bottomScrolled
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.extensions.hideKeyboard
import uikit.extensions.topScrolled
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FooterViewEmpty
import uikit.widget.HeaderViewSimple
import uikit.widget.ProcessTaskView

class SwapConfirmScreen(
    private val swapScreen: SwapAmountScreen
) : BaseFragment(R.layout.fragment_swap_confirm), BaseFragment.BottomSheet {
    private val swapScreenViewModel: SwapScreenViewModel by viewModel(ownerProducer = { swapScreen })
    private val sender by lazy { TransactionSender.FragmentSenderController(this,  swapScreenViewModel.transactionSender) }

    private lateinit var headerView: HeaderViewSimple
    private lateinit var listView: BlurredScrollView
    private lateinit var footerView: FooterViewEmpty

    private lateinit var tokenInputLayout1: SwapTokenInputLayoutView
    private lateinit var tokenInputLayout2: SwapTokenInputLayoutView
    private lateinit var swapInfoLayoutView: SwapInfoLayoutView

    private lateinit var buttonsLayout: LinearLayoutCompat
    private lateinit var sendButton: Button
    private lateinit var cancelButton: Button
    private lateinit var processView: ProcessTaskView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = {
            getCurrentFocus()?.hideKeyboard()
            finish()
        }
        headerView.setColor(requireContext().backgroundTransparentColor)

        listView = view.findViewById(R.id.list)
        listView.blurredPaddingTop = 64.dp
        listView.blurredPaddingBottom = 88.dp

        footerView = view.findViewById(R.id.footer)
        footerView.setColor(requireContext().backgroundTransparentColor)

        tokenInputLayout1 = view.findViewById(R.id.token_container_1)
        tokenInputLayout2 = view.findViewById(R.id.token_container_2)

        tokenInputLayout1.maxView.isVisible = false
        tokenInputLayout2.maxView.isVisible = false

        tokenInputLayout1.amountInput.isEnabled = false
        tokenInputLayout2.amountInput.isEnabled = false

        swapInfoLayoutView = view.findViewById(R.id.swap_info_layout)
        swapInfoLayoutView.background = TopDividerDrawable(view.context)

        cancelButton = view.findViewById(R.id.cancel)
        cancelButton.setOnClickListener { finish() }
        buttonsLayout = view.findViewById(R.id.buttons)
        processView = view.findViewById(R.id.process)

        sendButton = view.findViewById(R.id.next)
        sendButton.setOnClickListener {
            swapScreenViewModel.send(requireContext())
        }

        collectFlow(swapScreenViewModel.simulatedSwapToConfirmFlow.filterNotNull()) { swap ->
            tokenInputLayout1.amountInput.text = Editable.Factory.getInstance()
                .newEditable(swap.tokenToSend.units.toString(swap.tokenToSend.token.decimals))
            tokenInputLayout2.amountInput.text = Editable.Factory.getInstance()
                .newEditable(swap.tokenToReceive.units.toString(swap.tokenToReceive.token.decimals))

            tokenInputLayout1.balanceView.text = swap.tokenToSend.rate?.let { rate ->
                CurrencyFormatter.formatFiat(
                    rate.currency.code,
                    rate.rate * swap.tokenToSend.units.toFloat(swap.tokenToSend.token.decimals)
                )
            }

            tokenInputLayout2.balanceView.text = swap.tokenToReceive.rate?.let { rate ->
                CurrencyFormatter.formatFiat(
                    rate.currency.code,
                    rate.rate * swap.tokenToReceive.units.toFloat(swap.tokenToReceive.token.decimals)
                )
            }

            tokenInputLayout1.chooseTokenView.setToken(swap.tokenToSend.token)
            tokenInputLayout2.chooseTokenView.setToken(swap.tokenToReceive.token)
            swapInfoLayoutView.set(swap)
        }

        collectFlow(swapScreenViewModel.transactionSender.statusFlow) { state ->
            if (state.processActive) {
                buttonsLayout.visibility = View.GONE
                processView.visibility = View.VISIBLE
                processView.state = state.processState
            } else {
                buttonsLayout.visibility = View.VISIBLE
                processView.visibility = View.GONE
            }
        }

        collectFlow(listView.topScrolled, headerView::setDivider)
        collectFlow(listView.bottomScrolled, footerView::setDivider)

        sender.attach { close ->
            if (close.navigateToHistory) {
                navigation?.openURL("tonkeeper://activity")
            }
            swapScreen.forceFinish()
            finish()
        }
    }

    override fun getViewForNestedScrolling(): View = listView

    companion object {
        fun newInstance(swapScreen: SwapAmountScreen) = SwapConfirmScreen(swapScreen)
    }
}