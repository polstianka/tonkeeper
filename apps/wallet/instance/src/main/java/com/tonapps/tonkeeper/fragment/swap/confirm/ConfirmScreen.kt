package com.tonapps.tonkeeper.fragment.swap.confirm

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.tonapps.tonkeeper.fragment.swap.currency.CurrencyScreenState
import com.tonapps.tonkeeper.fragment.swap.currency.list.SwapDetailsItem
import com.tonapps.tonkeeper.fragment.swap.pager.PagerScreen
import com.tonapps.tonkeeper.fragment.swap.view.SendReceiveView
import com.tonapps.tonkeeperx.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.ProcessTaskView
import uikit.widget.RowLayout

class ConfirmScreen: PagerScreen(R.layout.fragment_swap_confirm) {

    companion object {
        fun newInstance() = ConfirmScreen()
    }

    private val feature: ConfirmScreenFeature by viewModel()

    private lateinit var sendReceiveView: SendReceiveView
    private lateinit var loadingView: View
    private lateinit var buttonsView: RowLayout
    private lateinit var cancelView: Button
    private lateinit var confirmView: Button
    private lateinit var processView: ProcessTaskView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sendReceiveView = view.findViewById(R.id.send_receive)
        loadingView = view.findViewById(R.id.loading)
        buttonsView = view.findViewById(R.id.buttons)
        cancelView = view.findViewById(R.id.cancel)
        confirmView = view.findViewById(R.id.confirm)
        processView = view.findViewById(R.id.process)


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                swapFeature.resultState.collect { state -> state?.let(::fillState) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                feature.uiState.collect { state -> state.let(::updateState) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                feature.uiEffect.collect { effect ->
                    when (effect) {
                        ConfirmScreenEffect.Success -> setSuccess()
                        ConfirmScreenEffect.Fail -> setFailed()
                    }
                }
            }
        }

        sendReceiveView.onSwapDetailsItemClick = { item ->
            when (item) {
                is SwapDetailsItem.Cell -> item.additionalInfo?.let { swapFeature.showMessage(getString(it)) }
                else -> Unit
            }
        }

        sendReceiveView.swapButtonVisible = false
        sendReceiveView.balanceActionAvailable = false

        confirmView.setOnClickListener { feature.confirm() }
        cancelView.setOnClickListener { (parent as? BaseFragment)?.finish() }
    }

    private fun fillState(state: CurrencyScreenState) {
        feature.resultState = state
        sendReceiveView.apply {
            enterTextEnabled = false
            sendToken = state.sendInfo.token
            receiveToken = state.receiveInfo.token
            setSendValue(state.sendInfo.amount.toString())
            setReceiveValue(state.receiveInfo.amount.toString())

            details = state.details.items.filterIsInstance<SwapDetailsItem.Cell>()

            sendReceiveView.balanceVisible = false
        }
    }

    private fun updateState(state: ConfirmScreenState) {
        if (state.loading) {
            buttonsView.visibility = View.GONE
            processView.visibility = View.VISIBLE
            processView.state = ProcessTaskView.State.LOADING
        }
    }

    private suspend fun setFailed() = withContext(Dispatchers.Main) {
        buttonsView.visibility = View.GONE
        processView.visibility = View.VISIBLE
        processView.state = ProcessTaskView.State.FAILED
        delay(2500)
        swapFeature.finish()
    }

    private suspend fun setSuccess() = withContext(Dispatchers.Main) {
        buttonsView.visibility = View.GONE
        processView.visibility = View.VISIBLE
        processView.state = ProcessTaskView.State.SUCCESS
        delay(2000)
        navigation?.openURL("tonkeeper://activity")
        delay(300)
        swapFeature.finish()
    }
}