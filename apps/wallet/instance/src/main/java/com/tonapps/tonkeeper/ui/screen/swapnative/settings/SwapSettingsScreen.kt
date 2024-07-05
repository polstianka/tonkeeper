package com.tonapps.tonkeeper.ui.screen.swapnative.settings

import android.os.Bundle
import android.view.View
import android.widget.Button
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.ui.screen.swapnative.main.SlippageSelectionListener
import com.tonapps.tonkeeperx.R
import uikit.base.BaseFragment
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.HeaderView
import uikit.widget.PercentInputView
import uikit.widget.SwitchView

class SwapSettingsScreen : BaseFragment(R.layout.fragment_swap_settings), BaseFragment.BottomSheet {

    private val args: SlippageSettingsArgs by lazy { SlippageSettingsArgs(requireArguments()) }

    private var slippageSelectionListener: SlippageSelectionListener? = null

    private lateinit var headerView: HeaderView
    private lateinit var slippagePercentInput: PercentInputView
    private lateinit var firstSlippageButton: Button
    private lateinit var secondSlippageButton: Button
    private lateinit var thirdslippageButton: Button
    private lateinit var switch: SwitchView
    private lateinit var saveButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerView = view.findViewById(R.id.header)
        slippagePercentInput = view.findViewById(R.id.slippage_percent_input)
        saveButton = view.findViewById(R.id.save)
        switch = view.findViewById(R.id.check)

        firstSlippageButton = view.findViewById<Button?>(R.id.first_slippage_button).apply {
            text = "$FIRST_SLIPPAGE_OPTION %"
            setOnClickListener { slippagePercentInput.text = "$FIRST_SLIPPAGE_OPTION" }
        }
        secondSlippageButton = view.findViewById<Button?>(R.id.second_slippage_button).apply {
            text = "$SECOND_SLIPPAGE_OPTION %"
            setOnClickListener { slippagePercentInput.text = "$SECOND_SLIPPAGE_OPTION" }
        }
        thirdslippageButton = view.findViewById<Button?>(R.id.third_slippage_button).apply {
            text = "$THIRD_SLIPPAGE_OPTION %"
            setOnClickListener { slippagePercentInput.text = "$THIRD_SLIPPAGE_OPTION" }
        }

        headerView.doOnActionClick = { finish() }

        slippagePercentInput.hint = getString(com.tonapps.wallet.localization.R.string.custom)
        slippagePercentInput.doOnTextChange = {
        }

        switch.doCheckedChanged = { isChecked ->
        }

        saveButton.setOnClickListener {
            val slippage = slippagePercentInput.getInputAsFloat()
            if (!requiresExpertMode(slippage) || switch.checked) {
                slippageSelectionListener?.onSlippageSelected(slippage)
                finish()
            } else if (!switch.checked) {
                navigation?.toast(getString(com.tonapps.wallet.localization.R.string.expert_mode_error))
            }
        }

        args.slippage.also { selectedSlippage ->
            slippagePercentInput.text = selectedSlippage.toString()
            switch.checked = requiresExpertMode(selectedSlippage)
        }

    }

    fun setBottomSheetDismissListener(slippageSelectionListener: SlippageSelectionListener) {
        this.slippageSelectionListener = slippageSelectionListener
    }

    private fun requiresExpertMode(percentAmount: Float): Boolean = percentAmount >= 50f

    companion object {
        fun newInstance(selectedSlippage: Float): SwapSettingsScreen {
            val fragment = SwapSettingsScreen()
            fragment.arguments = SlippageSettingsArgs(selectedSlippage).toBundle()
            return fragment
        }

        const val FIRST_SLIPPAGE_OPTION = 1f
        const val SECOND_SLIPPAGE_OPTION = 3f
        const val THIRD_SLIPPAGE_OPTION = 5f
    }

}