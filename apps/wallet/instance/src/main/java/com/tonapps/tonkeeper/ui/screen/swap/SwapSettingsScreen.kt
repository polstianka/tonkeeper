package com.tonapps.tonkeeper.ui.screen.swap

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.textPrimaryColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        input = view.findViewById(R.id.tolerance_input)
        header = view.findViewById(R.id.header)
        header.onCloseClick = { finish() }

        suggestions = view.findViewById(R.id.suggested_tolerance)

        input.isHintVisible = false
        input.editText.addTextChangedListener(PercentTextWatcher(input.editText, settingsViewModel))
        input.inputType = EditorInfo.TYPE_CLASS_NUMBER

        val defaultBg = ContextCompat.getDrawable(requireContext(), uikit.R.drawable.bg_text_border)
        val activeBg =
            ContextCompat.getDrawable(requireContext(), uikit.R.drawable.bg_text_border_active)

        collectFlow(settingsViewModel.suggestedTolerance) { sug ->
            val lp = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                1.0f
            )
            sug.forEachIndexed { index, percent ->
                suggestions.addView(createTextView(index, sug, lp, percent, defaultBg))
            }
        }

        collectFlow(settingsViewModel.selectedSuggestion) { percent ->
            setBackgroundForItems(defaultBg, percent, activeBg)
        }
    }

    private fun setBackgroundForItems(
        defaultBg: Drawable?,
        percent: Int?,
        activeBg: Drawable?
    ) {
        for (index in 0..<suggestions.childCount) {
            val child = suggestions.getChildAt(index)
            child.background = defaultBg
            if (child.tag == percent) child.background = activeBg
        }
        if (percent != null) {
            input.text = percent.toString()
        } else {
            input.clear()
        }
    }

    private fun createTextView(
        index: Int,
        sug: List<Int>,
        lp: LinearLayoutCompat.LayoutParams,
        percent: Int,
        defaultBg: Drawable?
    ): AppCompatTextView {
        return AppCompatTextView(requireContext()).apply {
            if (index != sug.lastIndex) {
                lp.setMargins(0, 0, 12.dp, 0)
            }
            tag = percent
            layoutParams = lp
            background = defaultBg
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