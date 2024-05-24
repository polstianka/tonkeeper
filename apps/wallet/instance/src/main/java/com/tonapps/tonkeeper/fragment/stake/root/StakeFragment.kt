package com.tonapps.tonkeeper.fragment.stake.root

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import coil.transform.RoundedCornersTransformation
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.loadUri
import com.tonapps.tonkeeper.core.toString
import com.tonapps.tonkeeper.fragment.stake.confirm.ConfirmStakeFragment
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import com.tonapps.tonkeeper.fragment.stake.pick_option.PickStakingOptionFragment
import com.tonapps.tonkeeper.fragment.stake.pool_details.PoolDetailsFragment.Companion.REQUEST_KEY_PICK_POOL
import com.tonapps.tonkeeper.fragment.stake.pool_details.PoolDetailsFragmentResult
import com.tonapps.tonkeeper.fragment.stake.ui.StakeInputView
import com.tonapps.tonkeeperx.R
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
        fun newInstance(
            pool: StakingPool? = null,
            service: StakingService? = null
        ) = StakeFragment().apply {
            setArgs(
                StakeArgs(pool, service)
            )
        }
    }

    private val viewModel: StakeViewModel by viewModel()
    private val header: HeaderView?
        get() = view?.findViewById(R.id.fragment_stake_header)
    private val optionIconView: ImageView?
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
    private val input: StakeInputView?
        get() = view?.findViewById(R.id.fragment_stake_input)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation?.setFragmentResultListener(REQUEST_KEY_PICK_POOL) { bundle ->
            val result = PoolDetailsFragmentResult(bundle)
            viewModel.onPoolPicked(result)
        }
        if (savedInstanceState == null) {
            viewModel.provideArgs(StakeArgs(requireArguments()))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header?.doOnActionClick = { viewModel.onCloseClicked() }
        header?.doOnCloseClick = { viewModel.onInfoClicked() }

        input?.setOnAmountChangedListener { viewModel.onAmountChanged(it) }
        input?.setOnMaxClickedListener { viewModel.onMaxClicked() }

        optionDropdown?.setThrottleClickListener { viewModel.onDropdownClicked() }

        button?.setThrottleClickListener { viewModel.onButtonClicked() }

        footer?.applyNavBottomPadding()

        observeFlow(viewModel.events, ::handleEvent)
        observeFlow(viewModel.fiatAmount) { input?.setFiatText(it.toString()) }
        observeFlow(viewModel.labelText) { input?.setLabelText(toString(it)) }
        observeFlow(viewModel.labelTextColorAttribute) { input?.setLabelTextColorAttribute(it) }
        observeFlow(viewModel.iconUri) {
            optionIconView?.loadUri(it, RoundedCornersTransformation(22f.dp))
        }
        observeFlow(viewModel.optionTitle) { optionTitle?.text = it }
        observeFlow(viewModel.optionSubtitle) { optionSubtitle?.text = toString(it) }
        observeFlow(viewModel.isMaxApy) { optionChip?.isVisible = it }
        observeFlow(viewModel.isButtonActive) { button?.isEnabled = it }
        observeFlow(viewModel.isMaxGlowing) { input?.setMaxButtonActivated(it) }
    }

    private fun handleEvent(event: StakeEvent) {
        when (event) {
            StakeEvent.NavigateBack -> finish()
            StakeEvent.ShowInfo -> navigation?.openURL("https://ton.org/stake", true)
            is StakeEvent.SetInputValue -> event.handle()
            is StakeEvent.PickStakingOption -> event.handle()
            is StakeEvent.NavigateToConfirmFragment -> event.handle()
        }
    }

    private fun StakeEvent.NavigateToConfirmFragment.handle() {
        val fragment = ConfirmStakeFragment.newInstance(pool, amount, type, isSendAll)
        navigation?.add(fragment)
    }

    private fun StakeEvent.SetInputValue.handle() {
        input?.setInputText(CurrencyFormatter.format(value, 2))
    }

    private fun StakeEvent.PickStakingOption.handle() {
        val fragment = PickStakingOptionFragment.newInstance(items, picked, currency)
        navigation?.add(fragment)
    }
}