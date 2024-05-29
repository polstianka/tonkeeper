package com.tonapps.tonkeeper.ui.screen.dialog.encrypted

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.viewModels
import com.tonapps.tonkeeper.ui.screen.swap.SwapArgs
import com.tonapps.tonkeeperx.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.widget.CheckBoxView
import uikit.widget.HeaderView

class EncryptedCommentScreen: BaseFragment(R.layout.fragment_encrypted_comment), BaseFragment.Modal {

    private val args: EncryptedCommentArgs by lazy { EncryptedCommentArgs(requireArguments()) }
    private val encryptedCommentViewModel: EncryptedCommentViewModel by viewModel()

    private lateinit var headerView: HeaderView
    private lateinit var button: Button
    private lateinit var checkboxContainerView: View
    private lateinit var checkboxView: CheckBoxView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.doOnActionClick = { finish() }

        button = view.findViewById(R.id.decrypted_button)
        button.setOnClickListener {
            encryptedCommentViewModel.decrypt(requireContext(), args.cipherText, args.senderAddress)
        }

        checkboxContainerView = view.findViewById(R.id.checkbox_container)
        checkboxView = view.findViewById(R.id.checkbox)

        checkboxContainerView.setOnClickListener { checkboxView.toggle() }
    }

    companion object {

        fun newInstance(cipherText: String, senderAddress: String): EncryptedCommentScreen {
            val fragment = EncryptedCommentScreen()
            fragment.setArgs(EncryptedCommentArgs(cipherText, senderAddress))
            return fragment
        }
    }
}