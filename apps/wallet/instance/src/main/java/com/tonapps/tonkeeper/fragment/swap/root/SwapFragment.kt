package com.tonapps.tonkeeper.fragment.swap.root

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.tonkeeper.extensions.doOnAmountChange
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import uikit.base.BaseFragment
import uikit.widget.HeaderView
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.extensions.setThrottleClickListener
import uikit.widget.ProcessTaskView
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
    private val sendAssetIcon: SimpleDraweeView?
        get() = view?.findViewById(R.id.fragment_swap_new_send_icon)
    private val sendAssetText: TextView?
        get() = view?.findViewById(R.id.fragment_swap_new_send_token_text)
    private val sendGroup: View?
        get() = view?.findViewById(R.id.fragment_swap_new_send_group)
    private val sendButton: View?
        get() = view?.findViewById(R.id.fragment_swap_new_send_token_button)
    private val receiveButton: View?
        get() = view?.findViewById(R.id.fragment_swap_new_receive_token_button)
    private val swapButton: View?
        get() = view?.findViewById(R.id.fragment_swap_new_swap_button)
    private val receiveAssetIcon: SimpleDraweeView?
        get() = view?.findViewById(R.id.fragment_swap_new_receive_icon)
    private val receiveAssetText: TextView?
        get() = view?.findViewById(R.id.fragment_swap_new_receive_token_text)
    private val sendInput: AmountInput?
        get() = view?.findViewById(R.id.fragment_swap_new_send_input)
    private val receiveInput: AmountInput?
        get() = view?.findViewById(R.id.fragment_swap_new_receive_input)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header?.doOnCloseClick = { viewModel.onSettingsClicked() }
        header?.doOnActionClick = { viewModel.onCrossClicked() }

        sendButton?.setThrottleClickListener { viewModel.onSendTokenClicked() }

        receiveButton?.setThrottleClickListener { viewModel.onReceiveTokenClicked() }

        swapButton?.setThrottleClickListener { viewModel.onSwapTokensClicked() }

        sendInput?.doOnAmountChange { viewModel.onSendAmountChanged(it) }

        observeFlow(viewModel.events) { handleEvent(it) }
        observeFlow(viewModel.isLoading) { updateLoading(it) }
        observeFlow(viewModel.pickedSendAsset) { updateSendAsset(it) }
        observeFlow(viewModel.pickedReceiveAsset) { updateReceiveAsset(it) }
    }

    private fun updateReceiveAsset(asset: DexAsset?) {
        receiveAssetIcon?.isVisible = asset != null
        asset?.imageUrl?.let { receiveAssetIcon?.setImageURI(it) }
        val text = asset?.displayName ?: getString(LocalizationR.string.choose)
        receiveAssetText?.text = text
    }

    private fun updateSendAsset(asset: DexAsset?) {
        sendAssetIcon?.isVisible = asset != null
        asset?.imageUrl?.let { sendAssetIcon?.setImageURI(it) }
        val text = asset?.displayName ?: getString(LocalizationR.string.choose)
        sendAssetText?.text = text
    }

    private fun updateLoading(isLoading: Boolean) {
        Log.wtf("###", "isLoading: $isLoading")
        loaderView?.isVisible = isLoading
        sendGroup?.isVisible = !isLoading
    }

    private fun handleEvent(event: SwapEvent) {
        when (event) {
            SwapEvent.NavigateBack -> finish()
        }
    }
}