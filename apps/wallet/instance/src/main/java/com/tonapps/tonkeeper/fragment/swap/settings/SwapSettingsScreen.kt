package com.tonapps.tonkeeper.fragment.swap.settings

import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.widget.Button
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.widget.doOnTextChanged
import com.tonapps.tonkeeper.ui.component.BlurredScrollView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundTransparentColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.drawable.InputDrawable
import uikit.extensions.bottomScrolled
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.extensions.focusWithKeyboard
import uikit.extensions.hideKeyboard
import uikit.extensions.topScrolled
import uikit.widget.FooterViewEmpty
import uikit.widget.HeaderViewSimple
import uikit.widget.SwitchView

class SwapSettingsScreen : BaseFragment(R.layout.fragment_swap_settings), BaseFragment.BottomSheet,
    View.OnFocusChangeListener, OnClickListener {
    private val settingsViewModel: SwapSettingsViewModel by viewModel()

    private lateinit var optionButtons: Array<View>
    private lateinit var inputDrawables: Array<InputDrawable>
    private lateinit var inputCustomSlippage: AppCompatEditText
    private lateinit var inputPriceImpact: AppCompatEditText
    private lateinit var exportModeContainer: LinearLayoutCompat
    private lateinit var switchExprtModeView: SwitchView
    private lateinit var nextButton: Button

    private lateinit var headerView: HeaderViewSimple
    private lateinit var listView: BlurredScrollView
    private lateinit var footerView: FooterViewEmpty


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }
        headerView.setColor(requireContext().backgroundTransparentColor)

        listView = view.findViewById(R.id.list)
        listView.blurredPaddingTop = 64.dp
        listView.blurredPaddingBottom = 88.dp

        footerView = view.findViewById(R.id.footer)
        footerView.setColor(requireContext().backgroundTransparentColor)

        optionButtons = arrayOf(
            view.findViewById(R.id.slippage_option_1),
            view.findViewById(R.id.slippage_option_2),
            view.findViewById(R.id.slippage_option_3),
            view.findViewById(R.id.slippage_option_custom_container),
            view.findViewById(R.id.price_impact_option_custom_container)
        )

        inputDrawables = arrayOf(
            InputDrawable(view.context),
            InputDrawable(view.context),
            InputDrawable(view.context),
            InputDrawable(view.context),
            InputDrawable(view.context),
        )

        for (i in 0..4) {
            optionButtons[i].background = inputDrawables[i]
            optionButtons[i].setOnClickListener(this)
        }

        inputPriceImpact = view.findViewById(R.id.price_impact_option_custom)
        inputCustomSlippage = view.findViewById(R.id.slippage_option_custom)
        switchExprtModeView = view.findViewById(R.id.check)
        exportModeContainer = view.findViewById(R.id.expert_mode_container)
        nextButton = view.findViewById(R.id.next)

        collectFlow(settingsViewModel.stateFlow, this::setState)
        collectFlow(listView.topScrolled, headerView::setDivider)
        collectFlow(listView.bottomScrolled, footerView::setDivider)
    }

    private fun setState(state: SwapSettingsViewModel.State) {
        if (finished) {
            return
        }

        if (state.force) {
            if (state.useCustomSlippage) {
                inputCustomSlippage.text = Editable.Factory.getInstance()
                    .newEditable(state.slippage?.times(100).toString())
            }
            inputPriceImpact.text = Editable.Factory.getInstance()
                .newEditable(state.customPriceImpact?.times(100).toString())
        }

        val inputPriceImpactFocused = state.priceImpactFocused
        val inputCustomSlippageFocused = state.useCustomSlippage && !state.priceImpactFocused

        updateHint(inputPriceImpact)
        updateHint(inputCustomSlippage)
        if (!state.force) {
            updateFocus(inputPriceImpact, inputPriceImpactFocused)
            updateFocus(inputCustomSlippage, inputCustomSlippageFocused)
        }

        inputDrawables[3].error = !inputCustomSlippageFocused && state.slippageError
        inputDrawables[4].error = !inputPriceImpactFocused && state.priceImpactError
        inputDrawables[4].active = state.priceImpactFocused
        for (i in 0..3) {
            inputDrawables[i].active = i == state.slippageIndex
        }

        switchExprtModeView.checked = state.priceImpactVisible
        exportModeContainer.visibility = if (state.priceImpactVisible) VISIBLE else GONE

        nextButton.isEnabled = state.valid

        initListeners()
    }

    private var initialized: Boolean = false
    private var finished: Boolean = false

    private fun initListeners() {
        if (initialized) {
            return
        }
        initialized = true

        inputPriceImpact.onFocusChangeListener = this
        inputPriceImpact.doOnTextChanged { text, _, _, _ ->
            settingsViewModel.onChangePriceImpactValue(text.toString().toFloatOrNull()?.div(100f))
        }

        inputCustomSlippage.onFocusChangeListener = this
        inputCustomSlippage.doOnTextChanged { text, _, _, _ ->
            settingsViewModel.onChangeSlippageValue(
                text.toString().toFloatOrNull()?.div(100f),
                true
            )
        }

        switchExprtModeView.doCheckedChanged = settingsViewModel::onChangePriceImpactVisible
        nextButton.setOnClickListener {
            finished = true
            getCurrentFocus()?.hideKeyboard()
            settingsViewModel.save()
            finish()
        }

    }

    override fun onClick(v: View?) {
        for (i in optionButtons.indices) {
            if (v === optionButtons[i]) {
                if (i == 3 || i == 4) {
                    onFocusChange(v, true)
                } else {
                    settingsViewModel.onChangeSlippageValue(
                        SwapSettingsViewModel.State.SLIPPAGE_VALUES[i],
                        false
                    )
                }
            }
        }
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (hasFocus && (v?.id == R.id.slippage_option_custom || v?.id == R.id.slippage_option_custom_container)) {
            settingsViewModel.onChangeSlippageValue(
                inputCustomSlippage.text.toString().toFloatOrNull(), true
            )
            /*if (!inputCustomSlippage.isFocused) {
                inputCustomSlippage.focusWithKeyboard()
            }*/
        } else if (v?.id == R.id.price_impact_option_custom || v?.id == R.id.price_impact_option_custom_container) {
            settingsViewModel.onChangePriceImpactFocused(hasFocus)
            /*if (hasFocus && !inputPriceImpact.isFocused) {
                inputPriceImpact.focusWithKeyboard()
            }*/
        }
    }

    override fun getViewForNestedScrolling(): View = listView

    override fun onDragging() {
        super.onDragging()
        getCurrentFocus()?.hideKeyboard()
    }

    companion object {
        fun newInstance() = SwapSettingsScreen()

        fun updateFocus(view: AppCompatEditText, focus: Boolean) {
            if (view.isFocused != focus) {
                if (focus) {
                    view.focusWithKeyboard()
                } else {
                    view.clearFocus()
                }
            }
        }

        fun updateHint(view: AppCompatEditText) {
            if (view.text.isNullOrEmpty()) {
                view.hint =
                    view.context.getString(com.tonapps.wallet.localization.R.string.swap_settings_custom_hint)
            } else {
                view.hint = null
            }
        }
    }
}