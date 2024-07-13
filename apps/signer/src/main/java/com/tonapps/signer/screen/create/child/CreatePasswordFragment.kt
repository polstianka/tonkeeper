package com.tonapps.signer.screen.create.child

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import com.tonapps.signer.R
import com.tonapps.signer.password.Password
import com.tonapps.signer.screen.create.CreateViewModel
import com.tonapps.signer.screen.create.pager.PageType
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.getViewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.extensions.pinToBottomInsets
import uikit.extensions.setPaddingTop
import uikit.widget.password.PasswordInputView

class CreatePasswordFragment : BaseFragment(R.layout.fragment_create_password) {

    companion object {
        private const val IS_REPEAT_KEY = "is_repeat"

        fun newInstance(isRepeat: Boolean): CreatePasswordFragment {
            val fragment = CreatePasswordFragment()
            fragment.arguments = Bundle().apply {
                putBoolean(IS_REPEAT_KEY, isRepeat)
            }
            return fragment
        }
    }

    private val isRepeat: Boolean by lazy { arguments?.getBoolean(IS_REPEAT_KEY) ?: false }

    private val pageType: PageType
        get() = if (isRepeat) PageType.RepeatPassword else PageType.Password

    private val createViewModel: CreateViewModel by lazy {
        requireParentFragment().getViewModel()
    }

    private lateinit var contentView: View
    private lateinit var titleView: AppCompatTextView
    private lateinit var limitView: View
    private lateinit var passwordInput: PasswordInputView
    private lateinit var doneButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentView = view.findViewById(R.id.content)

        passwordInput = view.findViewById(R.id.password)

        titleView = view.findViewById(R.id.title)
        if (isRepeat) {
            titleView.setText(R.string.re_enter_password)
        } else {
            titleView.setText(R.string.create_password)
        }

        limitView = view.findViewById(R.id.limit)
        if (isRepeat) {
            limitView.visibility = View.GONE
        }

        doneButton = view.findViewById(R.id.done)
        doneButton.setOnClickListener { sendPassword() }
        doneButton.pinToBottomInsets()

        passwordInput.doAfterValueChanged {
            if (it.isEmpty()) {
                doneButton.isEnabled = false
                return@doAfterValueChanged
            }
            passwordInput.error = false
            doneButton.isEnabled = Password.isValid(it)
        }

        collectFlow(createViewModel.page(pageType)) {
            passwordInput.error = false
            passwordInput.focusWithKeyboard()
        }

        if (isRepeat) {
            collectFlow(createViewModel.page(PageType.Password)) {
                passwordInput.clear()
            }
        }

        collectFlow(createViewModel.uiTopOffset) {
            contentView.setPaddingTop(it)
        }
    }

    private fun sendPassword() {
        val password = passwordInput.value
        if (!Password.isValid(password)) {
            setError()
            return
        }

        if (!isRepeat) {
            createViewModel.setPassword(password)
        } else if (!createViewModel.checkPassword(password)) {
            setError()
        }
    }

    private fun setError() {
        doneButton.isEnabled = false
        passwordInput.failedPassword()
    }

}