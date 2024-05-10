package com.tonapps.tonkeeper.fragment.trade.buy

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.fragment.trade.buy.vm.BuyViewModel
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment

class BuyFragment : BaseFragment(R.layout.fragment_buy) {
    companion object {
        fun newInstance() = BuyFragment()
    }

    private val viewModel: BuyViewModel by viewModel()
    private val input: AmountInput?
        get() = view?.findViewById(R.id.value)
    private val rateTextView: AppCompatTextView?
        get() = view?.findViewById(R.id.rate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        input?.doOnTextChanged { text, _, _, _ ->
            viewModel.onAmountChanged(text.toString())
        }
        observeFlow(viewModel.totalFiat) { rateTextView?.text = it }
    }
}