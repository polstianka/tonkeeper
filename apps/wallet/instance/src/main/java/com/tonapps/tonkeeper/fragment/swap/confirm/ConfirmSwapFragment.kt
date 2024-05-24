package com.tonapps.tonkeeper.fragment.swap.confirm

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.extensions.popBackToRootFragment
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSettings
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSimulation
import com.tonapps.tonkeeper.fragment.swap.root.SwapFragment
import com.tonapps.tonkeeper.fragment.swap.ui.SwapDetailsView
import com.tonapps.tonkeeper.fragment.swap.ui.SwapTokenButton
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.entity.RatesEntity
import core.extensions.observeFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.setThrottleClickListener
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.ModalHeader
import uikit.widget.ProcessTaskView
import java.math.BigDecimal

class ConfirmSwapFragment : BaseFragment(R.layout.fragment_swap_confirm), BaseFragment.BottomSheet {

    companion object {
        fun newInstance(
            sendToken: DexAsset,
            receiveToken: DexAsset,
            amount: BigDecimal,
            settings: SwapSettings,
            simulation: SwapSimulation.Result,
            currency: WalletCurrency,
            ratesEntity: RatesEntity,
            ratesUsd: RatesEntity
        ) = ConfirmSwapFragment().apply {
            setArgs(
                ConfirmSwapArgs(
                    sendToken,
                    receiveToken,
                    settings,
                    amount,
                    simulation,
                    currency,
                    ratesEntity = ratesEntity,
                    ratesUsd = ratesUsd
                )
            )
        }
    }

    private val viewModel: ConfirmSwapViewModel by viewModel()
    private val header: ModalHeader?
        get() = view?.findViewById(R.id.fragment_swap_confirm_header)
    private val sendAmountFiatTextView: TextView?
        get() = view?.findViewById(R.id.fragment_swap_confirm_send_amount_fiat)
    private val sendTokenButton: SwapTokenButton?
        get() = view?.findViewById(R.id.fragment_swap_confirm_send_token_button)
    private val sendAmountCryptoTextView: TextView?
        get() = view?.findViewById(R.id.fragment_swap_confirm_send_amount_crypto)
    private val receiveAmountFiatTextView: TextView?
        get() = view?.findViewById(R.id.fragment_swap_confirm_receive_amount_fiat)
    private val receiveTokenButton: SwapTokenButton?
        get() = view?.findViewById(R.id.fragment_swap_confirm_receive_token_button)
    private val receiveAmountCryptoTextView: TextView?
        get() = view?.findViewById(R.id.fragment_swap_confirm_receive_amount_crypto)
    private val swapDetailsView: SwapDetailsView?
        get() = view?.findViewById(R.id.fragment_swap_confirm_details)
    private val confirmButton: Button?
        get() = view?.findViewById(R.id.fragment_swap_confirm_button_positive)
    private val cancelButton: Button?
        get() = view?.findViewById(R.id.fragment_swap_confirm_button_negative)
    private val footer: View?
        get() = view?.findViewById(R.id.fragment_swap_confirm_footer)
    private val buttonsGroup: View?
        get() = view?.findViewById(R.id.fragment_swap_confirm_buttons_group)
    private val loader: ProcessTaskView?
        get() = view?.findViewById(R.id.fragment_swap_confirm_loader)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.provideArgs(
                ConfirmSwapArgs(requireArguments())
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header?.onCloseClick = { viewModel.onCloseClicked() }

        footer?.applyNavBottomPadding()

        confirmButton?.setThrottleClickListener { viewModel.onConfirmClicked() }

        cancelButton?.setThrottleClickListener { viewModel.onCancelClicked() }

        observeFlow(viewModel.events) { handleEvent(it) }
        observeFlow(viewModel.args) { updateState(it) }
        observeFlow(viewModel.isLoading) { isLoading ->
            buttonsGroup?.isVisible = !isLoading
            loader?.isVisible = isLoading
        }
        observeFlow(viewModel.loaderState) { loader?.state = it }
    }

    private fun updateState(args: ConfirmSwapArgs) {
        val sendAmountUsd = args.sendAsset.dexUsdPrice * args.amount

        val tonToCurrency = args.ratesEntity.getRate("TON")
        val tonToUsd = args.ratesUsd.getRate("TON")
        val sendAmountCurrency = sendAmountUsd / tonToUsd * tonToCurrency
        val sendAmountFiatText = CurrencyFormatter.format(args.currency.code, sendAmountCurrency)

        sendAmountFiatTextView?.text = sendAmountFiatText
        sendTokenButton?.asset = args.sendAsset
        val sendAmountCrypto = CurrencyFormatter.format(args.amount, 2)
        sendAmountCryptoTextView?.text = sendAmountCrypto

        receiveAmountFiatTextView?.text = sendAmountFiatText
        receiveTokenButton?.asset = args.receiveAsset
        val receiveAmountCrypto = args.amount * args.sendAsset.dexUsdPrice / args.receiveAsset.dexUsdPrice
        receiveAmountCryptoTextView?.text = CurrencyFormatter.format(receiveAmountCrypto, 2)
        swapDetailsView?.updateState(args.simulation)
    }

    private fun handleEvent(event: ConfirmSwapEvent) {
        when (event) {
            ConfirmSwapEvent.NavigateBack -> finish()
            is ConfirmSwapEvent.FinishFlow -> event.handle()
        }
    }

    private fun ConfirmSwapEvent.FinishFlow.handle() {
        popBackToRootFragment(includingRoot = true, SwapFragment::class)
        finish()
        if (navigateToHistory) {
            navigation?.openURL("tonkeeper://activity")
        }
    }
}