package com.tonapps.tonkeeper.fragment.stake

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.toString
import com.tonapps.tonkeeper.extensions.doOnAmountChange
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.resolveColor
import core.extensions.observeFlow
import uikit.base.BaseFragment
import uikit.widget.HeaderView
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.extensions.setThrottleClickListener

class StakeFragment : BaseFragment(R.layout.fragment_stake), BaseFragment.BottomSheet {

    companion object {
        fun newInstance() = StakeFragment()
    }

    private val viewModel: StakeViewModel by viewModel()
    private val header: HeaderView?
        get() = view?.findViewById(R.id.fragment_stake_header)
    private val input: AmountInput?
        get() = view?.findViewById(R.id.fragment_stake_input)
    private val fiatTextView: TextView?
        get() = view?.findViewById(R.id.fragment_stake_fiat)
    private val maxButton: View?
        get() = view?.findViewById(R.id.fragment_stake_max)
    private val availableLabel: TextView?
        get() = view?.findViewById(R.id.fragment_stake_available)
    private val optionIconView: SimpleDraweeView?
        get() = view?.findViewById(R.id.fragment_stake_option_icon)
    private val optionTitle: TextView?
        get() = view?.findViewById(R.id.fragment_stake_option_title)
    private val optionSubtitle: TextView?
        get() = view?.findViewById(R.id.fragment_stake_option_subtitle)
    private val optionChip: View?
        get() = view?.findViewById(R.id.fragment_stake_option_chip)
    private val optionDropdown: View?
        get() = view?.findViewById(R.id.fragment_stake_dropdown)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header?.setIcon(R.drawable.ic_info_16)
        header?.doOnActionClick = { viewModel.onCloseClicked() }
        header?.doOnCloseClick = { viewModel.onInfoClicked() }

        input?.doOnAmountChange { viewModel.onAmountChanged(it) }

        maxButton?.setThrottleClickListener { viewModel.onMaxClicked() }

        optionDropdown?.setThrottleClickListener { viewModel.onDropdownClicked() }

        observeFlow(viewModel.events, ::handleEvent)
        observeFlow(viewModel.fiatAmount) { fiatTextView?.text = it }
        observeFlow(viewModel.labelText) { availableLabel?.text = toString(it) }
        observeFlow(viewModel.labelTextColorAttribute) { attr ->
            availableLabel?.setTextColor(requireContext().resolveColor(attr))
        }
    }

    private fun handleEvent(event: StakeEvent) {
        when (event) {
            StakeEvent.NavigateBack -> finish()
            StakeEvent.ShowInfo -> Log.wtf("###", "showInfo")
            is StakeEvent.SetInputValue -> event.handle()
        }
    }

    private fun StakeEvent.SetInputValue.handle() {
        input?.setText(CurrencyFormatter.formatFloat(value, 2))
    }
}