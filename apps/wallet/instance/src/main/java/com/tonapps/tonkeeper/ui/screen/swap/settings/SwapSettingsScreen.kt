package com.tonapps.tonkeeper.ui.screen.swap.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import com.tonapps.tonkeeper.ui.screen.swap.pager.PagerScreen
import com.tonapps.tonkeeper.ui.screen.swap.pager.SwapScreenAdapter
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.dp
import uikit.widget.SwitchView


class SwapSettingsScreen :
    PagerScreen<SwapSettingsScreenState, SwapSettingsScreenEffect, SwapSettingsScreenFeature>(R.layout.fragment_swap_settings),
    BaseFragment.CustomBackground {

    companion object {
        fun newInstance() = SwapSettingsScreen()
    }

    override val feature: SwapSettingsScreenFeature by viewModel()

    private lateinit var continueButton: Button
    private lateinit var buttonPerc1: FrameLayout
    private lateinit var buttonPerc2: FrameLayout
    private lateinit var buttonPerc3: FrameLayout
    private lateinit var custom: View
    private lateinit var customEdit: AppCompatEditText
    private lateinit var buttonsPerc: LinearLayoutCompat
    private lateinit var expertMode: View
    private lateinit var expertModeSwitch: SwitchView
    private lateinit var close: View
    private lateinit var percent: View

    private var skipTextChangeListener = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        close = view.findViewById(R.id.close)
        close.setOnClickListener {
            swapFeature.setCurrentPage(SwapScreenAdapter.POSITION_AMOUNT)
        }

        buttonPerc1 = view.findViewById(R.id.button_perc1)
        buttonPerc2 = view.findViewById(R.id.button_perc2)
        buttonPerc3 = view.findViewById(R.id.button_perc3)

        buttonPerc1.setOnClickListener {
            customEdit.clearFocus()
            feature.setSlippage(0.01f)
        }
        buttonPerc2.setOnClickListener {
            customEdit.clearFocus()
            feature.setSlippage(0.03f)
        }
        buttonPerc3.setOnClickListener {
            customEdit.clearFocus()
            feature.setSlippage(0.05f)
        }
        percent = view.findViewById(R.id.percent)
        custom = view.findViewById(R.id.custom)
        customEdit = view.findViewById(R.id.custom_edit)

        custom.setOnClickListener {
            customEdit.requestFocus()
        }

        customEdit.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            try {
                val input = dest.toString() + source.toString()
                if (input.toInt() > 50) {
                    ""
                } else {
                    source
                }
            } catch (e: NumberFormatException) {
                ""
            }
        })

        customEdit.setOnFocusChangeListener(object : OnFocusChangeListener {
            override fun onFocusChange(p0: View?, b: Boolean) {
                custom.isSelected = b
            }
        })

        customEdit.doOnTextChanged { text, start, before, count ->
            percent.animate().cancel()

            percent.animate().translationX(
                if (customEdit.text.toString().isEmpty()) 75.dp.toFloat() else 7.dp + customEdit.text.toString().length * 10.dp.toFloat()
            ).setDuration(50).start()
        }

        customEdit.doAfterTextChanged {
            if (!skipTextChangeListener) {
                if (it.toString().isEmpty()) {
                    feature.setSlippage(0.01f)
                } else {
                    feature.setSlippage(it.toString().toFloat() / 100, true)
                }
            }
        }

        expertModeSwitch = view.findViewById(R.id.check)
        expertMode = view.findViewById(R.id.expert_mode)
        expertMode.setOnClickListener {
            expertModeSwitch.checked = !expertModeSwitch.checked
        }
        expertModeSwitch.doCheckedChanged = {
            if (!skipTextChangeListener) {
                feature.setExpertMode(it)
            }
        }

        buttonsPerc = view.findViewById(R.id.buttons_perc)

        continueButton = view.findViewById(R.id.continue_action)
        continueButton.setOnClickListener {
            if (feature.uiState.value.error) {
                custom.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.shake))
            } else {
                swapFeature.setSettings(
                    feature.uiState.value.slippage,
                    feature.uiState.value.expertMode
                )
                feature.save()
            }
        }
    }

    private fun swapViews(editFirst: Boolean) {
        if (editFirst) {
            if (custom.translationY == 0f) {
                custom.animate().cancel()
                custom.animate().translationY(-70.dp.toFloat()).setDuration(200).start()

                buttonsPerc.animate().cancel()
                buttonsPerc.animate().translationY(72.dp.toFloat()).setDuration(200).start()
            }
        } else {
            if (custom.translationY < 0) {
                custom.animate().cancel()
                custom.animate().translationY(0f).setDuration(200).start()

                buttonsPerc.animate().cancel()
                buttonsPerc.animate().translationY(0f).setDuration(200).start()
            }
        }
    }

    override fun newUiState(state: SwapSettingsScreenState) {
        if (state.saved) {
            swapFeature.setCurrentPage(SwapScreenAdapter.POSITION_AMOUNT)
            return
        }
        custom.isActivated = state.error
        skipTextChangeListener = true
        expertModeSwitch.checked = state.expertMode
        swapViews(state.asText)
        if (state.asText) {
            customEdit.setText("")
            customEdit.append((state.slippage * 100f).toInt().toString())
            buttonPerc1.isSelected = false
            buttonPerc2.isSelected = false
            buttonPerc3.isSelected = false
        } else {
            //custom.clearFocus()
            when (state.slippage) {
                0.01f -> {
                    buttonPerc1.isSelected = true
                    buttonPerc2.isSelected = false
                    buttonPerc3.isSelected = false
                }

                0.03f -> {
                    buttonPerc2.isSelected = true
                    buttonPerc3.isSelected = false
                    buttonPerc1.isSelected = false
                }

                0.05f -> {
                    buttonPerc3.isSelected = true
                    buttonPerc2.isSelected = false
                    buttonPerc1.isSelected = false
                }
            }
            customEdit.setText("")
        }
        skipTextChangeListener = false
    }

    override fun onVisibleChange(visible: Boolean) {
        super.onVisibleChange(visible)
        if (visible) {
            swapFeature.swap.value?.let {
                //feature.setData(it)
            }
        }
    }
}