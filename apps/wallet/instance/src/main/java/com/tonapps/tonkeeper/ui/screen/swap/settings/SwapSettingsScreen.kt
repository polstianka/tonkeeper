package com.tonapps.tonkeeper.ui.screen.swap.settings

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import androidx.core.view.forEachIndexed
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tonapps.tonkeeper.core.measuredHeightWithVerticalMargins
import com.tonapps.tonkeeper.core.updateInsetPaddingBottom
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.ui.screen.swap.data.SlippageTolerance
import com.tonapps.tonkeeper.ui.screen.swap.data.SwapSettings
import com.tonapps.tonkeeper.ui.screen.swap.data.percentageToSlippageTolerance
import com.tonapps.tonkeeper.ui.screen.swap.data.percentageToSlippageToleranceOrNull
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import uikit.base.BaseFragment
import uikit.extensions.doKeyboardAnimation
import uikit.extensions.hideKeyboard
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.AutoDisableNestedScrollView
import uikit.widget.BottomSheetLayout
import uikit.widget.HeaderView
import uikit.widget.InputView
import uikit.widget.SelectableTextView
import uikit.widget.item.ItemSwitchView

class SwapSettingsScreen : BaseFragment(R.layout.fragment_swap_settings), BaseFragment.BottomSheet {
    companion object {
        const val RESULT_KEY = "new_settings"

        fun newInstance(requestKey: String, currentSettings: SwapSettings): SwapSettingsScreen {
            val screen = SwapSettingsScreen()
            screen.setArgs(SwapSettingsArgs(requestKey, currentSettings), ignoreErrors = true)
            return screen
        }
    }

    private val args: SwapSettingsArgs by lazy {
        lazyArgs as? SwapSettingsArgs ?: SwapSettingsArgs(requireArguments())
    }

    private val settingsViewModel: SwapSettingsViewModel by viewModel { parametersOf(args.currentSettings)  }

    private lateinit var headerView: HeaderView
    private lateinit var labelAction: ViewGroup
    private lateinit var contentView: AutoDisableNestedScrollView

    private lateinit var customSlippageView: InputView

    private lateinit var percentageSuggestionsView: LinearLayoutCompat
    private lateinit var slippageToleranceSuggestions: List<SlippageTolerance>

    private lateinit var expertSwitchView: ItemSwitchView

    private var behavior: BottomSheetBehavior<FrameLayout>? = null
    override fun onPrepareToShow(parent: BottomSheetLayout, behavior: BottomSheetBehavior<FrameLayout>) {
        this.behavior = behavior
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerView = view.findViewById(R.id.header)
        headerView.contentMatchParent()
        headerView.doOnActionClick = {
            alertOrFinish()
        }
        headerView.alignTitleToStart()

        contentView = view.findViewById(R.id.content)
        contentView.onTopScrolledStateChanged = { hasScrolled ->
            behavior?.isHideable = !hasScrolled
        }

        labelAction = view.findViewById(R.id.label_action)
        view.doKeyboardAnimation { offset, progress, isShowing, navigationBarSize ->
            labelAction.translationY = -offset.toFloat()
            contentView.updateInsetPaddingBottom(offset, progress, isShowing, navigationBarSize, labelAction.measuredHeightWithVerticalMargins)
        }
        val labelButton: Button = labelAction.findViewById(R.id.label_button)
        labelButton.setOnClickListener {
            val newSettings = settingsViewModel.uiState.value
            val bundle = Bundle().apply {
                putParcelable(RESULT_KEY, newSettings)
            }
            setFragmentResult(args.requestKey, bundle)
            navigation?.toast(Localization.swap_settings_saved)
            finish()
        }

        expertSwitchView = view.findViewById(R.id.expert)
        expertSwitchView.checked = args.currentSettings.enableExpertMode
        expertSwitchView.doOnCheckedChanged = {
            settingsViewModel.setEnableExpertMode(it)
        }

        percentageSuggestionsView = view.findViewById(R.id.percentage_suggestions)
        val slippageToleranceSuggestions = mutableListOf<SlippageTolerance>()
        percentageSuggestionsView.children.forEach {
            require(it is SelectableTextView)
            val slippageTolerance = it.text.toString().replace(Regex("[^0-9.]+"), "").percentageToSlippageTolerance()
            slippageToleranceSuggestions += slippageTolerance
            val active = args.currentSettings.slippageTolerance == slippageTolerance
            it.forceActive(active)
            it.setOnClickListener { _ ->
                selectSuggestedSlippageTolerance(slippageTolerance)
            }
        }
        this.slippageToleranceSuggestions = slippageToleranceSuggestions

        customSlippageView = view.findViewById(R.id.percent)
        customSlippageView.reductionAnimationType = InputView.ReductionAnimationType.STICKY_SUFFIX
        customSlippageView.suffix = " % "
        if (!slippageToleranceSuggestions.contains(args.currentSettings.slippageTolerance)) {
            customSlippageView.text = args.currentSettings.slippageTolerance.movePointRight(2).stripTrailingZeros().toPlainString()
            customSlippageView.forceActive(true)
        }
        customSlippageView.activateDrawableOnFocus = false
        customSlippageView.doOnTextChange = {
            selectCustomSlippageTolerance(it)
        }

        settingsViewModel.viewModelScope.launch {
            settingsViewModel.uiState.collect { newSettings ->
                percentageSuggestionsView.forEachIndexed { index, child ->
                    require(child is SelectableTextView)
                    val suggestedPercentage = slippageToleranceSuggestions[index]
                    child.active = newSettings.slippageTolerance == suggestedPercentage
                }
                val pendingCustomSlippageTolerance = customSlippageView.text.percentageToSlippageToleranceOrNull() ?: SlippageTolerance.ZERO
                customSlippageView.active = pendingCustomSlippageTolerance == newSettings.slippageTolerance
            }
        }
    }

    private var ignoreTextChanges: Boolean = false

    private fun selectSuggestedSlippageTolerance(slippageTolerance: SlippageTolerance) {
        getCurrentFocus()?.hideKeyboard()
        if ((customSlippageView.text.toFloatOrNull() ?: 0.0) != slippageTolerance) {
            ignoreTextChanges = true
            customSlippageView.clearInput()
            ignoreTextChanges = false
        }
        settingsViewModel.setSlippageTolerance(slippageTolerance)
    }

    private fun selectCustomSlippageTolerance(customSlippageText: String) {
        if (ignoreTextChanges) return
        val customSlippage = customSlippageText.percentageToSlippageToleranceOrNull() ?: SlippageTolerance.ZERO
        val isValid = customSlippage > SlippageTolerance.ZERO && customSlippage <= SwapSettings.MAX_SLIPPAGE_TOLERANCE
        customSlippageView.error = customSlippageText.isNotEmpty() && !isValid
        val currentSlippageTolerance = settingsViewModel.uiState.value.slippageTolerance
        val newSlippage = when {
            isValid -> customSlippage
            // percentageSuggestions.contains(currentSlippageTolerance) -> currentSlippageTolerance
            else -> SwapSettings.DEFAULT_SLIPPAGE_TOLERANCE
        }
        customSlippageView.active = customSlippage == currentSlippageTolerance
        settingsViewModel.setSlippageTolerance(newSlippage)
    }

    override fun onBackPressed(): Boolean {
        alertOrFinish()
        return false
    }

    private val hasUnsavedChanges: Boolean
        get() = args.currentSettings != settingsViewModel.uiState.value

    override fun onDragging() {
        super.onDragging()
        getCurrentFocus()?.hideKeyboard()
    }

    private fun alertOrFinish() {
        if (hasUnsavedChanges) {
            // TODO: show some alert?
            // These settings seems to be not critical to warn about losing changes, so I didn't do it
        }
        hideKeyboardAndRun(::finish)
    }
}