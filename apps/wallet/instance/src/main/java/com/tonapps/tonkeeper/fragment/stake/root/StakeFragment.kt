package com.tonapps.tonkeeper.fragment.stake.root

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import com.facebook.drawee.view.SimpleDraweeView
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.toString
import com.tonapps.tonkeeper.extensions.doOnAmountChange
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.fragment.stake.confirm.ConfirmStakeFragment
import com.tonapps.tonkeeper.fragment.stake.pick_option.PickStakingOptionFragment
import com.tonapps.tonkeeper.fragment.stake.pool_details.PoolDetailsFragment.Companion.REQUEST_KEY_PICK_POOL
import com.tonapps.tonkeeper.fragment.stake.pool_details.PoolDetailsFragmentResult
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.resolveColor
import core.extensions.observeFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.dp
import uikit.extensions.setThrottleClickListener
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView

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
    private val button: Button?
        get() = view?.findViewById(R.id.fragment_stake_button)
    private val footer: View?
        get() = view?.findViewById(R.id.fragment_stake_footer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation?.setFragmentResultListener(REQUEST_KEY_PICK_POOL) { bundle ->
            val result = PoolDetailsFragmentResult(bundle)
            viewModel.onPoolPicked(result)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header?.doOnActionClick = { viewModel.onCloseClicked() }
        header?.doOnCloseClick = { viewModel.onInfoClicked() }

        input?.doOnAmountChange { viewModel.onAmountChanged(it) }

        maxButton?.setThrottleClickListener { viewModel.onMaxClicked() }

        optionDropdown?.setThrottleClickListener { viewModel.onDropdownClicked() }

        button?.setThrottleClickListener { viewModel.onButtonClicked() }

        footer?.applyNavBottomPadding(32f.dp)

        observeFlow(viewModel.events, ::handleEvent)
        observeFlow(viewModel.fiatAmount) { fiatTextView?.text = it }
        observeFlow(viewModel.labelText) { availableLabel?.text = toString(it) }
        observeFlow(viewModel.labelTextColorAttribute) { attr ->
            availableLabel?.setTextColor(requireContext().resolveColor(attr))
        }
        observeFlow(viewModel.iconUrl) { optionIconView?.setImageURI(it) }
        observeFlow(viewModel.optionTitle) { optionTitle?.text = it }
        observeFlow(viewModel.optionSubtitle) { optionSubtitle?.text = toString(it) }
        observeFlow(viewModel.isMaxApy) { optionChip?.isVisible = it }
    }

    private fun handleEvent(event: StakeEvent) {
        when (event) {
            StakeEvent.NavigateBack -> finish()
            StakeEvent.ShowInfo -> Log.wtf("###", "showInfo")
            is StakeEvent.SetInputValue -> event.handle()
            is StakeEvent.PickStakingOption -> event.handle()
            is StakeEvent.NavigateToConfirmFragment -> event.handle()
        }
    }

    private fun StakeEvent.NavigateToConfirmFragment.handle() {
        val fragment = ConfirmStakeFragment.newInstance(pool, amount, type)
        navigation?.add(fragment)
    }

    private fun StakeEvent.SetInputValue.handle() {
        input?.setText(CurrencyFormatter.format(value, value.scale()))
    }

    private fun StakeEvent.PickStakingOption.handle() {
        val fragment = PickStakingOptionFragment.newInstance(items, picked, currency)
        navigation?.add(fragment)
    }
}