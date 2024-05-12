package com.tonapps.tonkeeper.fragment.trade.pick_currency

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.tonapps.tonkeeperx.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.setThrottleClickListener

class PickCurrencyFragment : BaseFragment(R.layout.fragment_pick_currency), BaseFragment.BottomSheet {

    companion object {
        fun newInstance(
            paymentMethodId: String,
            pickedCurrencyCode: String?
        ): PickCurrencyFragment {
            val args = PickCurrencyFragmentArgs(paymentMethodId, pickedCurrencyCode)
            return PickCurrencyFragment().apply { setArgs(args) }
        }
    }

    private val viewModel: PickCurrencyViewModel by viewModel()
    private val cross: View?
        get() = view?.findViewById(R.id.fragment_pick_currency_header_cross)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            viewModel.provideArgs(PickCurrencyFragmentArgs(requireArguments()))
        }
        lifecycleScope.launch {
            viewModel.events.collectLatest { handleEvent(it) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cross?.setThrottleClickListener { viewModel.onCrossClicked() }
    }

    private fun handleEvent(event: PickCurrencyEvent) {
        when (event) {
            PickCurrencyEvent.NavigateBack -> finish()
        }
    }
}