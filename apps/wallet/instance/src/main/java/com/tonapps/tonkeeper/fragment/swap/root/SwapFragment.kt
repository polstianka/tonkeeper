package com.tonapps.tonkeeper.fragment.swap.root

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.extensions.doOnAmountChange
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.fragment.swap.domain.model.AssetBalance
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSimulation
import com.tonapps.tonkeeper.fragment.swap.domain.model.formatCurrency
import com.tonapps.tonkeeper.fragment.swap.pick_asset.PickAssetFragment
import com.tonapps.tonkeeper.fragment.swap.pick_asset.PickAssetResult
import com.tonapps.tonkeeper.fragment.swap.settings.SwapSettingsFragment
import com.tonapps.tonkeeper.fragment.swap.settings.SwapSettingsResult
import com.tonapps.tonkeeper.fragment.swap.ui.SwapTokenButton
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import uikit.base.BaseFragment
import uikit.widget.HeaderView
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.setThrottleClickListener
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.ProcessTaskView
import java.math.BigDecimal
import com.tonapps.wallet.localization.R as LocalizationR

class SwapFragment : BaseFragment(R.layout.fragment_swap_new), BaseFragment.BottomSheet {

    companion object {
        fun newInstance() = SwapFragment()
    }

    private val viewModel: SwapViewModel by viewModel()
    private val header: HeaderView?
        get() = view?.findViewById(R.id.fragment_swap_new_header)
    private val loaderView: ProcessTaskView?
        get() = view?.findViewById(R.id.fragment_swap_new_process_task_view)
    private val sendGroup: View?
        get() = view?.findViewById(R.id.fragment_swap_new_send_group)
    private val sendButton: View?
        get() = view?.findViewById(R.id.fragment_swap_new_send_token_button)
    private val receiveButton: SwapTokenButton?
        get() = view?.findViewById(R.id.fragment_swap_new_receive_token_button)
    private val swapButton: View?
        get() = view?.findViewById(R.id.fragment_swap_new_swap_button)
    private val sendInput: AmountInput?
        get() = view?.findViewById(R.id.fragment_swap_new_send_input)
    private val receiveInput: TextView?
        get() = view?.findViewById(R.id.fragment_swap_new_receive_input)
    private val balanceTextView: TextView?
        get() = view?.findViewById(R.id.fragment_swap_new_balance_label)
    private val receiveGroup: View?
        get() = view?.findViewById(R.id.fragment_swap_new_receive_group)
    private val simulationGroup: View?
        get() = view?.findViewById(R.id.fragment_swap_new_simulation_group)
    private val simulationLoader: View?
        get() = view?.findViewById(R.id.fragment_swap_new_exchange_simulation_loader)
    private val simulationDetails: View?
        get() = view?.findViewById(R.id.fragment_swap_new_exchange_simulation_details)
    private val simulationDetailsExchangeRateTitle: TextView?
        get() = view?.findViewById(R.id.fragment_swap_new_exchange_simulation_details_exchange_rate_title)
    private val simulationDetailsExchangeRateValue: TextView?
        get() = view?.findViewById(R.id.fragment_swap_new_exchange_simulation_details_exchange_rate_value)
    private val simulationDetailsMinReceived: TextView?
        get() = view?.findViewById(R.id.fragment_swap_new_exchange_simulation_min_received)
    private val simulationDetailsLiquidityProviderFee: TextView?
        get() = view?.findViewById(R.id.fragment_swap_new_exchange_simulation_liquidity_provider_fee)
    private val simulationDetailsBlockchainFee: TextView?
        get() = view?.findViewById(R.id.fragment_swap_new_exchange_simulation_blockchain_fee)
    private val confirmButton: Button?
        get() = view?.findViewById(R.id.fragment_swap_new_confirm_button)
    private val footer: View?
        get() = view?.findViewById(R.id.fragment_swap_new_footer)
    private val sendTokenButton: SwapTokenButton?
        get() = view?.findViewById(R.id.fragment_swap_new_send_token_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation?.setFragmentResultListener(PickAssetResult.REQUEST_KEY) { bundle ->
            val result = PickAssetResult(bundle)
            viewModel.onAssetPicked(result)
        }
        navigation?.setFragmentResultListener(SwapSettingsResult.REQUEST_KEY) { bundle ->
            val result = SwapSettingsResult(bundle)
            viewModel.onSettingsUpdated(result)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header?.doOnCloseClick = { viewModel.onSettingsClicked() }
        header?.doOnActionClick = { viewModel.onCrossClicked() }

        sendButton?.setThrottleClickListener { viewModel.onSendTokenClicked() }

        receiveButton?.setThrottleClickListener { viewModel.onReceiveTokenClicked() }

        swapButton?.setThrottleClickListener { viewModel.onSwapTokensClicked() }

        sendInput?.doOnAmountChange { viewModel.onSendAmountChanged(it) }

        footer?.applyNavBottomPadding()

        confirmButton?.setThrottleClickListener { viewModel.onConfirmClicked() }

        observeFlow(viewModel.events) { handleEvent(it) }
        observeFlow(viewModel.isLoading) { updateLoading(it) }
        observeFlow(viewModel.pickedSendAsset) { sendTokenButton?.asset = it }
        observeFlow(viewModel.pickedReceiveAsset) { receiveButton?.asset = it }
        observeFlow(viewModel.pickedTokenBalance) { updateBalance(it) }
        observeFlow(viewModel.receiveAmount) { pair ->
            val text = when {
                pair == null ->  ""
                else ->  CurrencyFormatter.format(pair.first.symbol, pair.second)
            }
            receiveInput?.text = text
        }
        observeFlow(viewModel.simulation) { it.updateSimulation() }
    }

    private fun SwapSimulation?.updateSimulation() {
        simulationGroup?.isVisible = this != null
        when (this) {
            SwapSimulation.Loading -> {
                simulationLoader?.isVisible = true
                simulationDetails?.isVisible = false
                confirmButton?.isActivated = false
                confirmButton?.isEnabled = false
            }
            is SwapSimulation.Result -> {
                simulationLoader?.isVisible = false
                simulationDetails?.isVisible = true
                simulationDetailsExchangeRateTitle?.text = CurrencyFormatter.format(
                    sentAsset.symbol,
                    BigDecimal.ONE
                )
                simulationDetailsExchangeRateValue?.text = CurrencyFormatter.format(
                    receivedAsset.symbol,
                    exchangeRate
                )
                simulationDetailsMinReceived?.text = CurrencyFormatter.format(
                    receivedAsset.symbol,
                    minimumReceivedAmount
                )
                simulationDetailsLiquidityProviderFee?.text = CurrencyFormatter.format(
                    receivedAsset.symbol,
                    liquidityProviderFee
                )
                simulationDetailsBlockchainFee?.text = CurrencyFormatter.format(
                    "TON",
                    blockchainFee
                )
                confirmButton?.isActivated = true
                confirmButton?.isEnabled = true
                confirmButton?.text = getString(
                    com.tonapps.wallet.localization.R.string.continue_action
                )
            }
            null -> {
                confirmButton?.isActivated = false
                confirmButton?.isEnabled = false
            }
        }
    }

    private fun updateBalance(balance: AssetBalance?) {
        when (balance) {
            is AssetBalance.Entity ->
                balanceTextView?.text = balance.balance
                    .formatCurrency(balance.asset)

            AssetBalance.Loading,
            null -> balanceTextView?.text = ""
        }
    }

    private fun updateLoading(isLoading: Boolean) {
        loaderView?.isVisible = isLoading
        sendGroup?.isVisible = !isLoading
        receiveGroup?.isVisible = !isLoading
        swapButton?.isVisible = !isLoading
    }

    private fun handleEvent(event: SwapEvent) {
        when (event) {
            SwapEvent.NavigateBack -> finish()
            is SwapEvent.NavigateToPickAsset -> event.handle()
            is SwapEvent.NavigateToSwapSettings -> event.handle()
            is SwapEvent.FillInput -> event.handle()
        }
    }

    private fun SwapEvent.FillInput.handle() {
        sendInput?.setText(text)
    }

    private fun SwapEvent.NavigateToSwapSettings.handle() {
        val fragment = SwapSettingsFragment.newInstance(settings)
        navigation?.add(fragment)
    }

    private fun SwapEvent.NavigateToPickAsset.handle() {
        val fragment = PickAssetFragment.newInstance(type)
        navigation?.add(fragment)
    }
}