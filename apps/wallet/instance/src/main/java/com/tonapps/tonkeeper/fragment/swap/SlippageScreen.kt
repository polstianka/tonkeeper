package com.tonapps.tonkeeper.fragment.swap

import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.fragment.app.viewModels
import com.tonapps.extensions.getParcelableCompat
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.fragment.swap.model.Slippage
import com.tonapps.tonkeeperx.R
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.widget.HeaderView
import uikit.widget.InputRoundedView
import uikit.widget.SelectButtonView

class SlippageScreen : BaseFragment(R.layout.fragment_slippage_settings), BaseFragment.BottomSheet {

    private val viewModel: SlippageViewModel by viewModels()

    private val separator = CurrencyFormatter.monetaryDecimalSeparator

    private val headerView by lazy(LazyThreadSafetyMode.NONE) {
        requireView().findViewById<HeaderView>(R.id.header)
    }

    private val valueCustom: InputRoundedView by lazy(LazyThreadSafetyMode.NONE) {
        requireView().findViewById(R.id.value_custom)
    }

    private val value1Percent: SelectButtonView by lazy(LazyThreadSafetyMode.NONE) {
        requireView().findViewById(R.id.value_1percent)
    }

    private val value3Percent: SelectButtonView by lazy(LazyThreadSafetyMode.NONE) {
        requireView().findViewById(R.id.value_3percent)
    }

    private val value5Percent: SelectButtonView by lazy(LazyThreadSafetyMode.NONE) {
        requireView().findViewById(R.id.value_5percent)
    }

    private val slippageArgument: Slippage by lazy(LazyThreadSafetyMode.NONE) {
        arguments?.getParcelableCompat(KEY_SLIPPAGE)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            viewModel.onSelect(slippageArgument)
        }

        headerView.doOnActionClick = { finish() }

        collectFlow(viewModel.selectedFlow) {
            value1Percent.isSelected = it == Slippage.Slippage1Percent
            value3Percent.isSelected = it == Slippage.Slippage3Percent
            value5Percent.isSelected = it == Slippage.Slippage5Percent
            valueCustom.isSelected = it is Slippage.SlippageCustom
        }

        value1Percent.setOnClickListener {
            viewModel.onSelect(Slippage.Slippage1Percent)
        }

        value3Percent.setOnClickListener {
            viewModel.onSelect(Slippage.Slippage3Percent)
        }

        value5Percent.setOnClickListener {
            viewModel.onSelect(Slippage.Slippage5Percent)
        }

        var text = valueCustom.text.toString()

        valueCustom.doOnTextChange = { editable ->
            val text = editable.toString()
            

            editable.replace(0, editable.length, text)
        }

        valueCustom.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->

        }
    }

    override fun onEndShowingAnimation() {

    }

    override fun onDragging() {

    }

    companion object {
        fun newInstance(slippage: Slippage): SlippageScreen = SlippageScreen().apply {
            arguments = Bundle().apply {
                putParcelable(KEY_SLIPPAGE, slippage)
            }
        }

        const val SLIPPAGE_REQUEST = "slippage_request"
        const val KEY_SLIPPAGE = "slippage"
    }
}