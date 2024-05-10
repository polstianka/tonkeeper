package com.tonapps.tonkeeper.fragment.trade.buy

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.fragment.trade.buy.vm.BuyViewModel
import com.tonapps.tonkeeper.fragment.trade.ui.rv.TradeAdapter
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.round
import uikit.extensions.setThrottleClickListener
import uikit.widget.SimpleRecyclerView

class BuyFragment : BaseFragment(R.layout.fragment_buy) {
    companion object {
        fun newInstance() = BuyFragment()
    }

    private val viewModel: BuyViewModel by viewModel()
    private val input: AmountInput?
        get() = view?.findViewById(R.id.value)
    private val rateTextView: AppCompatTextView?
        get() = view?.findViewById(R.id.rate)
    private val adapter = TradeAdapter { viewModel.onTradeMethodClicked(it) }
    private val recyclerView: SimpleRecyclerView?
        get() = view?.findViewById(R.id.fragment_buy_rv)
    private val button: Button?
        get() = view?.findViewById(R.id.next)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        input?.doOnTextChanged { text, _, _, _ ->
            viewModel.onAmountChanged(text.toString())
        }
        recyclerView?.adapter = adapter
        observeFlow(viewModel.totalFiat) { rateTextView?.text = it }
        observeFlow(viewModel.methods) { adapter.submitList(it) }
        observeFlow(viewModel.isButtonActive) { button?.isEnabled = it }
        // clip children ripple effect
        recyclerView?.round(resources.getDimensionPixelSize(uikit.R.dimen.cornerMedium))
        button?.setThrottleClickListener { viewModel.onButtonClicked() }
    }
}