package com.tonapps.tonkeeper.ui.screen.init.step

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.tonapps.tonkeeper.ui.screen.init.InitViewModel
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.getDimensionPixelSize
import uikit.widget.NumPadView
import uikit.widget.PinInputView

class PasscodeScreen: BaseFragment(R.layout.fragment_init_passcode)  {

    override val fragmentName: String = "PasscodeScreen"

    private val initViewModel: InitViewModel by viewModel(ownerProducer = { requireParentFragment() })
    private val reEnter: Boolean by lazy { requireArguments().getBoolean(ARG_RE_ENTER) }

    private lateinit var pinInputView: PinInputView
    private lateinit var numPadView: NumPadView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val titleView = view.findViewById<AppCompatTextView>(R.id.title)
        titleView.setText(if (reEnter) Localization.passcode_re_enter else Localization.passcode_create)

        pinInputView = view.findViewById(R.id.passcode)

        numPadView = view.findViewById(R.id.num_pad)
        numPadView.doOnBackspaceClick = {
            pinInputView.removeLastNumber()
            numPadView.backspace = pinInputView.count != 0
        }
        numPadView.doOnNumberClick = {
            pinInputView.appendNumber(it)
            numPadView.backspace = true
            if (pinInputView.count == 4) {
                setPasscode(pinInputView.code)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val insetsNav = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val bottom = insetsNav + requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium)
            numPadView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = bottom
            }
            insets
        }
    }

    private fun setPasscode(code: String) {
        if (reEnter) {
            initViewModel.reEnterPasscode(code)
        } else {
            initViewModel.setPasscode(code)
        }
    }

    companion object {
        private const val ARG_RE_ENTER = "re_enter"

        fun newInstance(reEnter: Boolean): PasscodeScreen {
            val fragment = PasscodeScreen()
            fragment.arguments = Bundle().apply {
                putBoolean(ARG_RE_ENTER, reEnter)
            }
            return fragment
        }
    }
}