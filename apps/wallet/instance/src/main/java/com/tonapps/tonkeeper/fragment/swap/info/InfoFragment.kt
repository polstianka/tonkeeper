package com.tonapps.tonkeeper.fragment.swap.info

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.tonapps.tonkeeperx.R
import core.extensions.observeFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.widget.ModalHeader

class InfoFragment : BaseFragment(R.layout.fragment_info), BaseFragment.Modal {

    companion object {
        fun newInstance(args: InfoArgs) = InfoFragment().apply { setArgs(args) }
        fun newInstance(title: String, text: String) = InfoFragment().apply {
            setArgs(
                InfoArgs(title, text)
            )
        }
    }

    private val viewModel: InfoViewModel by viewModel()
    private val header: ModalHeader?
        get() = view?.findViewById(R.id.fragment_info_header)
    private val body: TextView?
        get() = view?.findViewById(R.id.fragment_info_body)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.provideArgs(
                InfoArgs(
                    requireArguments()
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header?.onCloseClick = { viewModel.onCloseClicked() }

        body?.applyNavBottomPadding()

        observeFlow(viewModel.args) { updateState(it) }
        observeFlow(viewModel.events) { handleEvent(it) }
    }

    private fun handleEvent(event: InfoEvent) {
        when (event) {
            InfoEvent.Finish -> finish()
        }
    }

    private fun updateState(args: InfoArgs) {
        header?.text = args.title
        body?.text = args.text
    }
}