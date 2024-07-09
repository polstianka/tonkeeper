package com.tonapps.tonkeeper.fragment.swap.settings

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSettings
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.drawable.InputDrawable
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.applySelectableBgContent
import uikit.extensions.dp
import uikit.extensions.setThrottleClickListener
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.InputView
import uikit.widget.ModalHeader
import uikit.widget.SwitchView

class SwapSettingsFragment : BaseFragment(R.layout.fragment_swap_settings), BaseFragment.BottomSheet {

    companion object {
        fun newInstance(settings: SwapSettings) = SwapSettingsFragment().apply {
            setArgs(
                SwapSettingsArgs(settings)
            )
        }
    }

    private val viewModel: SwapSettingsViewModel by viewModel()
    private val header: ModalHeader?
        get() = view?.findViewById(R.id.fragment_swap_settings_header)
    private val footer: View?
        get() = view?.findViewById(R.id.fragment_swap_settings_footer)
    private val switch: SwitchView?
        get() = view?.findViewById(R.id.fragment_swap_settings_switch)
    private val slippageOptionOne: View?
        get() = view?.findViewById(R.id.fragment_swap_settings_slippage_1)
    private val slippageOptionTwo: View?
        get() = view?.findViewById(R.id.fragment_swap_settings_slippage_2)
    private val slippageOptionThree: View?
        get() = view?.findViewById(R.id.fragment_swap_settings_slippage_3)
    private val input: InputView?
        get() = view?.findViewById(R.id.fragment_swap_settings_input)
    private val expertModeGroup: View?
        get() = view?.findViewById(R.id.fragment_swap_settings_expert_mode_group)
    private val editText: EditText?
        get() = view?.findViewById(uikit.R.id.input_field)
    private val button: Button?
        get() = view?.findViewById(R.id.fragment_swap_settings_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.provideArgs(
                SwapSettingsArgs(requireArguments())
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header?.onCloseClick = { viewModel.onCloseClick() }

        footer?.applyNavBottomPadding(16f.dp)

        switch?.setOnClickListener(null)

        slippageOptionOne?.background = InputDrawable(requireContext())
        slippageOptionOne?.setThrottleClickListener { viewModel.onSlippageOptionOneChecked() }

        slippageOptionTwo?.background = InputDrawable(requireContext())
        slippageOptionTwo?.setThrottleClickListener { viewModel.onSlippageOptionTwoChecked() }

        slippageOptionThree?.background = InputDrawable(requireContext())
        slippageOptionThree?.setThrottleClickListener { viewModel.onSlippageOptionThreeChecked() }

        input?.doOnTextChange = { viewModel.onInputChanged(it) }

        editText?.imeOptions = EditorInfo.IME_ACTION_DONE
        editText?.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL + InputType.TYPE_CLASS_NUMBER

        expertModeGroup?.setThrottleClickListener { viewModel.onExpertModeChecked() }
        expertModeGroup?.applySelectableBgContent()

        button?.setOnClickListener { viewModel.onButtonClicked() }

        observeFlow(viewModel.events) { handleEvent(it) }
        observeFlow(viewModel.screenState) { handleState(it) }
    }

    private fun handleState(settings: SwapSettings) {
        when (settings) {
            is SwapSettings.ExpertMode -> {
                (slippageOptionOne?.background as? InputDrawable)?.active = false
                (slippageOptionTwo?.background as? InputDrawable)?.active = false
                (slippageOptionThree?.background as? InputDrawable)?.active = false
                input?.isEnabled = true
                input?.requestFocus()
                switch?.checked = true
            }
            is SwapSettings.NoviceMode -> {
                switch?.checked = false
                input?.clearFocus()
                input?.isEnabled = false
                when (settings) {
                    SwapSettings.NoviceMode.One -> {
                        (slippageOptionOne?.background as? InputDrawable)?.active = true
                        (slippageOptionTwo?.background as? InputDrawable)?.active = false
                        (slippageOptionThree?.background as? InputDrawable)?.active = false
                    }
                    SwapSettings.NoviceMode.Three -> {
                        (slippageOptionOne?.background as? InputDrawable)?.active = false
                        (slippageOptionTwo?.background as? InputDrawable)?.active = true
                        (slippageOptionThree?.background as? InputDrawable)?.active = false
                    }
                    SwapSettings.NoviceMode.Five -> {
                        (slippageOptionOne?.background as? InputDrawable)?.active = false
                        (slippageOptionTwo?.background as? InputDrawable)?.active = false
                        (slippageOptionThree?.background as? InputDrawable)?.active = true
                    }
                }
            }
        }
    }

    private fun handleEvent(event: SwapSettingsEvent) {
        when (event) {
            SwapSettingsEvent.NavigateBack -> finish()
            is SwapSettingsEvent.FillInput -> event.handle()
            is SwapSettingsEvent.ReturnResult -> event.handle()
        }
    }

    private fun SwapSettingsEvent.ReturnResult.handle() {
        val result = SwapSettingsResult(settings)
        navigation?.setFragmentResult(SwapSettingsResult.REQUEST_KEY, result.toBundle())
        finish()
    }

    private fun SwapSettingsEvent.FillInput.handle() {
        input?.text = text
    }
}