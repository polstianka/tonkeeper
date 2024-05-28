package com.tonapps.tonkeeper.fragment.swap.settings

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.tonapps.tonkeeper.fragment.send.view.AmountInput
import com.tonapps.tonkeeper.fragment.swap.pager.PagerScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.drawable.InputDrawable
import uikit.extensions.focusWithKeyboard
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.setPaddingHorizontal
import uikit.widget.HeaderView
import uikit.widget.amount.PercentInputFilter
import uikit.widget.item.ItemSwitchView

class SettingsScreen : PagerScreen(R.layout.fragment_swap_settings), BaseFragment.BottomSheet {

    companion object {
        fun newInstance() = SettingsScreen()
    }

    private val feature: SettingsScreenFeature by viewModel()

    private lateinit var headerView: HeaderView
    private lateinit var slippageView: View
    private lateinit var fieldView: AmountInput
    private lateinit var firstView: Button
    private lateinit var secondView: Button
    private lateinit var thirdView: Button
    private lateinit var expertModeView: ItemSwitchView
    private lateinit var saveView: Button

    private val firstDrawable by lazy { InputDrawable(requireContext()) }
    private val secondDrawable by lazy { InputDrawable(requireContext()) }
    private val thirdDrawable by lazy { InputDrawable(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerView = view.findViewById(R.id.title)
        val inputDrawable = InputDrawable(requireContext())
        slippageView = view.findViewById(R.id.slippage_percent)
        slippageView.background = inputDrawable
        slippageView.setOnClickListener { fieldView.focusWithKeyboard() }
        fieldView = view.findViewById(R.id.field)
        fieldView.background = null
        fieldView.setTextColor(requireContext().textSecondaryColor)
        fieldView.setTextAppearance(uikit.R.style.TextAppearance_Body1)
        fieldView.setPadding(
            requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium),
            requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium),
            requireContext().getDimensionPixelSize(uikit.R.dimen.offsetExtraSmall),
            requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium),
        )
        fieldView.filters = arrayOf(PercentInputFilter())
        fieldView.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            inputDrawable.active = hasFocus
        }
        fieldView.doOnTextChanged { text, _, _, _ ->
            val newValue = text?.toString().orEmpty()
            feature.onSlippageChanged(newValue)
        }

        firstView = view.findViewById(R.id.first)
        firstView.background = firstDrawable
        firstView.setOnClickListener { feature.onSlippageSuggestionClick(firstView.text.toString()) }
        secondView = view.findViewById(R.id.second)
        secondView.background = secondDrawable
        secondView.setOnClickListener { feature.onSlippageSuggestionClick(secondView.text.toString()) }
        thirdView = view.findViewById(R.id.third)
        thirdView.background = thirdDrawable
        thirdView.setOnClickListener { feature.onSlippageSuggestionClick(thirdView.text.toString()) }

        expertModeView = view.findViewById(R.id.expert_mode)
        expertModeView.doOnCheckedChanged = feature::expertModeCheckedChanged

        saveView = view.findViewById(R.id.save)
        saveView.setOnClickListener { feature.onSaveClick() }

        headerView = view.findViewById(R.id.title)
        headerView.setAction(com.tonapps.uikit.icon.R.drawable.ic_close_16)
        headerView.closeView.visibility = View.GONE
        val params = LinearLayoutCompat.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.START
        }
        headerView.titleView.layoutParams = params
        headerView.doOnActionClick = { finish() }
        headerView.findViewById<View>(uikit.R.id.header_text).setPaddingHorizontal(0)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                feature.uiState.collect(::fillState)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                feature.uiEffect.collect { effect ->
                    when (effect) {
                        is SettingsScreenEffect.UpdateSlippage -> fieldView.setText(effect.value)
                        is SettingsScreenEffect.Finish -> finish()
                    }
                }
            }
        }

        feature.load()
    }

    private fun fillState(state: SettingsScreenState) {
        state.suggestions.forEachIndexed { index, suggestion ->
            val value = getString(Localization.value_percent, suggestion.toString())
            when (index) {
                0 -> {
                    firstView.text = value
                    firstDrawable.active = suggestion.toFloat() == state.slippage
                }

                1 -> {
                    secondView.text = value
                    secondDrawable.active = suggestion.toFloat() == state.slippage
                }

                2 -> {
                    thirdView.text = value
                    thirdDrawable.active = suggestion.toFloat() == state.slippage
                }
            }
        }
        expertModeView.checked = state.expertMode
    }
}