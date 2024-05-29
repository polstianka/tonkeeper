package com.tonapps.tonkeeper.fragment.swap.root

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.TextWrapper
import com.tonapps.tonkeeper.core.toString
import com.tonapps.tonkeeper.extensions.doOnAmountChange
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.fragment.swap.confirm.ConfirmSwapFragment
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetBalance
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSimulation
import com.tonapps.tonkeeper.fragment.swap.pick_asset.PickAssetFragment
import com.tonapps.tonkeeper.fragment.swap.pick_asset.PickAssetResult
import com.tonapps.tonkeeper.fragment.swap.settings.SwapSettingsFragment
import com.tonapps.tonkeeper.fragment.swap.settings.SwapSettingsResult
import com.tonapps.tonkeeper.fragment.swap.ui.SwapDetailsView
import com.tonapps.tonkeeper.fragment.swap.ui.SwapTokenButton
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.dp
import uikit.extensions.setThrottleClickListener
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView
import uikit.widget.ProcessTaskView

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
    private val confirmButton: Button?
        get() = view?.findViewById(R.id.fragment_swap_new_confirm_button)
    private val footer: View?
        get() = view?.findViewById(R.id.fragment_swap_new_footer)
    private val sendTokenButton: SwapTokenButton?
        get() = view?.findViewById(R.id.fragment_swap_new_send_token_button)
    private val detailsView: SwapDetailsView?
        get() = view?.findViewById(R.id.fragment_swap_new_swap_details)
    private val maxButton: TextView?
        get() = view?.findViewById(R.id.fragment_swap_new_max_button)
    private val receiveBalance: TextView?
        get() = view?.findViewById(R.id.fragment_swap_new_receive_balance)

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

        footer?.applyNavBottomPadding(16f.dp)

        confirmButton?.setThrottleClickListener { viewModel.onConfirmClicked() }

        maxButton?.setThrottleClickListener { viewModel.onMaxClicked() }

        observeFlow(viewModel.events) { handleEvent(it) }
        observeFlow(viewModel.isLoading) { updateLoading(it) }
        observeFlow(viewModel.pickedSendAsset) { updatePickedSendAsset(it) }
        observeFlow(viewModel.pickedReceiveAsset) {
            receiveButton?.asset = it
            val text = if (it == null) {
                ""
            } else {
                CurrencyFormatter.format(it.symbol, it.balance)
            }
            receiveBalance?.text = text
        }
        observeFlow(viewModel.receiveAmount) { pair ->
            val text = when {
                pair == null ->  ""
                else ->  CurrencyFormatter.format(pair.first.symbol, pair.second)
            }
            receiveInput?.text = text
        }
        observeFlow(viewModel.simulation) { it.updateSimulation() }
        observeFlow(viewModel.buttonState) { updateButtonState(it) }
    }

    private fun updateButtonState(pair: Pair<TextWrapper.StringResource, Boolean>) {
        confirmButton?.isEnabled = pair.second
        confirmButton?.isActivated = pair.second
        confirmButton?.text = toString(pair.first)
    }

    private fun updatePickedSendAsset(asset: DexAssetBalance?) {
        sendTokenButton?.asset = asset
        balanceTextView?.isVisible = asset != null
        asset ?: return
        balanceTextView?.text = CurrencyFormatter.format(
            asset.symbol,
            asset.balance
        )
    }

    private fun SwapSimulation?.updateSimulation() {
        detailsView?.updateState(this)
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
            is SwapEvent.NavigateToConfirm -> event.handle()
        }
    }

    private fun SwapEvent.NavigateToConfirm.handle() {
        val fragment = ConfirmSwapFragment.newInstance(
            sendAsset,
            receiveAsset,
            amount,
            settings,
            simulation
        )
        navigation?.add(fragment)
    }

    private fun SwapEvent.FillInput.handle() {
        sendInput?.setText(text)
    }

    private fun SwapEvent.NavigateToSwapSettings.handle() {
        val fragment = SwapSettingsFragment.newInstance(settings)
        navigation?.add(fragment)
    }

    private fun SwapEvent.NavigateToPickAsset.handle() {
        val fragment = PickAssetFragment.newInstance(type, toSend, toReceive)
        navigation?.add(fragment)
    }
}