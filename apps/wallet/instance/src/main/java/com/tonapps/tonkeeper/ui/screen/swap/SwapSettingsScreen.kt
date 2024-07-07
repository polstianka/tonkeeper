package com.tonapps.tonkeeper.ui.screen.swap

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.setPadding
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.textPrimaryColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.drawable.InputDrawable
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.extensions.getDimensionPixelSize
import uikit.widget.InputView
import uikit.widget.ModalHeader
import uikit.widget.item.ItemSwitchViewExtended


class SwapSettingsScreen : BaseFragment(R.layout.fragment_swap_settings), BaseFragment.BottomSheet {

    private val settingsViewModel: SwapSettingsViewModel by viewModel()

    private lateinit var input: InputView
    private lateinit var expertSwitch: ItemSwitchViewExtended
    private lateinit var suggestions: LinearLayoutCompat
    private lateinit var header: ModalHeader
    private lateinit var saveButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        input = view.findViewById(R.id.tolerance_input)
        header = view.findViewById(R.id.header)
        header.onCloseClick = { finish() }

        suggestions = view.findViewById(R.id.suggested_tolerance)
        saveButton = view.findViewById(R.id.save)
        saveButton.setOnClickListener {
            settingsViewModel.onSaveClick()
            finish()
        }

        expertSwitch = view.findViewById(R.id.custom_tolerance)
        expertSwitch.doOnCheckedChanged = {
            settingsViewModel.onSwitchChanged(it)
        }

        input.isHintVisible = false
        input.editText.addTextChangedListener(PercentTextWatcher(input.editText, settingsViewModel))
        input.inputType = EditorInfo.TYPE_CLASS_NUMBER

        val suggestionsList = settingsViewModel.uiState.value.suggestedToleranceList
        suggestionsList.forEachIndexed { index, percent ->
            suggestions.addView(
                createTextView(
                    index = index,
                    lastIndex = suggestionsList.lastIndex,
                    lp = LinearLayoutCompat.LayoutParams(
                        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                        1.0f
                    ),
                    percent = percent,
                )
            )
        }

        collectFlow(settingsViewModel.uiState) { state ->
            setBackgroundForItems(state.tolerancePercent)
        }
    }

    private fun setBackgroundForItems(percent: Int?) {
        for (index in 0..<suggestions.childCount) {
            val child = suggestions.getChildAt(index)
            val bg = child.background as InputDrawable
            bg.active = child.tag == percent
        }
        if (percent != null) {
            input.text = percent.toString()
        } else {
            input.clear()
        }
    }

    private fun createTextView(
        index: Int,
        lastIndex: Int,
        lp: LinearLayoutCompat.LayoutParams,
        percent: Int,
    ): Button {
        val buttonStyle = uikit.R.style.Widget_App_Button_Secondary
        val backgroundDrawable = InputDrawable(requireContext())
        return Button(ContextThemeWrapper(context, buttonStyle), null, buttonStyle).apply {
            if (index != lastIndex) {
                lp.setMargins(0, 0, 12.dp, 0)
            }
            tag = percent
            background = backgroundDrawable
            layoutParams = lp
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            text = "$percent %"
            setPadding(context.getDimensionPixelSize(uikit.R.dimen.offsetMedium))
            setTextAppearance(uikit.R.style.TextAppearance_Body1)
            setTextColor(requireContext().textPrimaryColor)
            setOnClickListener { settingsViewModel.onSuggestClicked(percent) }
        }
    }

    companion object {
        fun newInstance() = SwapSettingsScreen()
    }
}

private class PercentTextWatcher(
    private val editText: EditText,
    private val viewModel: SwapSettingsViewModel,
) : TextWatcher {

    private val postfix = " %"

    init {
        editText.accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun sendAccessibilityEvent(host: View, eventType: Int) {
                super.sendAccessibilityEvent(host, eventType)
                if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
                    val selection = editText.text.toString().length - postfix.length
                    editText.setSelection(selection.coerceAtLeast(0))
                }
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(s: Editable) {
        val inputText = s.toString()
        val current = if (inputText.contains(postfix)) inputText.dropLast(2) else inputText
        val combined = "$current$postfix"
        viewModel.percentChanged(current)
        editText.removeTextChangedListener(this)
        editText.setText(combined)
        editText.setSelection(current.length)
        editText.addTextChangedListener(this)
    }
}