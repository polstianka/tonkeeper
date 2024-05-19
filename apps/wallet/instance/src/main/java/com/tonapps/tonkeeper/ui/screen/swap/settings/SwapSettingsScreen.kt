package com.tonapps.tonkeeper.ui.screen.swap.settings

import android.os.Bundle
import android.view.View
import android.widget.Button
import com.tonapps.tonkeeperx.R
import uikit.base.BaseFragment
import uikit.extensions.doKeyboardAnimation

class SwapSettingsScreen : BaseFragment(R.layout.fragment_swap_settings), BaseFragment.BottomSheet {
    private lateinit var saveButton: Button

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        saveButton = view.findViewById(R.id.save_button)
        // saveButton.pinToBottomInsets()

        view.doKeyboardAnimation { offset, _, _ ->
            saveButton.translationY = -offset.toFloat()
        }
    }
}
