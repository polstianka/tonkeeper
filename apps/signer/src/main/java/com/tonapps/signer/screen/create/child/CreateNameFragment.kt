package com.tonapps.signer.screen.create.child

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.tonapps.signer.R
import com.tonapps.signer.extensions.authorizationRequiredError
import com.tonapps.signer.screen.create.CreateViewModel
import com.tonapps.signer.screen.create.pager.PageType
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.getViewModel
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.extensions.pinToBottomInsets
import uikit.extensions.setPaddingTop
import uikit.widget.InputView
import uikit.widget.LoaderView

class CreateNameFragment: BaseFragment(R.layout.fragment_create_name) {

    companion object {
        fun newInstance() = CreateNameFragment()
    }

    private val createViewModel: CreateViewModel by lazy {
        requireParentFragment().getViewModel()
    }

    private lateinit var contentView: View
    private lateinit var nameInput: InputView
    private lateinit var actionView: View
    private lateinit var doneButton: Button
    private lateinit var loaderView: LoaderView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentView = view.findViewById(R.id.content)

        nameInput = view.findViewById(R.id.name)
        nameInput.setOnDoneActionListener { done() }

        actionView = view.findViewById(R.id.action)
        actionView.pinToBottomInsets()

        doneButton = view.findViewById(R.id.done)
        doneButton.setOnClickListener { done() }

        loaderView = view.findViewById(R.id.loader)

        nameInput.doOnTextChange = {
            doneButton.isEnabled = it.isNotBlank()
        }

        collectFlow(createViewModel.page(PageType.Name)) {
            nameInput.focus()
        }

        collectFlow(createViewModel.page(PageType.RepeatPassword)) {
            nameInput.clear()
        }

        collectFlow(createViewModel.uiTopOffset) {
            contentView.setPaddingTop(it)
        }
    }

    private fun done() {
        val name = nameInput.text
        if (name.isBlank()) {
            return
        }
        createViewModel.setName(name)
        collectFlow(createViewModel.addKey(requireContext())) { done ->
            if (!done) {
                applyDefaultState()
            }
        }
        applyLoadingState()
    }

    private fun applyLoadingState() {
        loaderView.visibility = View.VISIBLE
        doneButton.visibility = View.GONE
        nameInput.hideKeyboard()
    }

    private fun applyDefaultState() {
        loaderView.visibility = View.GONE
        doneButton.visibility = View.VISIBLE
    }
}