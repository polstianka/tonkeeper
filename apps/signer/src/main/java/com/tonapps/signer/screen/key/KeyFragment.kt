package com.tonapps.signer.screen.key

import android.os.Bundle
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.tonapps.signer.Key
import com.tonapps.signer.R
import com.tonapps.signer.core.entities.KeyEntity
import com.tonapps.signer.deeplink.TKDeepLink
import com.tonapps.signer.extensions.authorizationRequiredError
import com.tonapps.signer.extensions.copyToClipboard
import com.tonapps.signer.screen.name.NameFragment
import com.tonapps.signer.extensions.short8
import com.tonapps.signer.screen.phrase.PhraseFragment
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.ton.api.pub.PublicKeyEd25519
import com.tonapps.qr.ui.QRView
import com.tonapps.uikit.list.ListCell
import uikit.base.BaseFragment
import uikit.dialog.alert.AlertDialog
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.collectFlow
import uikit.extensions.drawable
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.round
import uikit.extensions.topScrolled
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.ActionCellView
import uikit.widget.HeaderView

class KeyFragment: BaseFragment(R.layout.fragment_key), BaseFragment.SwipeBack {

    companion object {

        fun newInstance(
            id: Long,
        ) = KeyFragment().apply {
            arguments = Bundle().apply {
                putLong(Key.ID, id)
            }
        }
    }

    private val id: Long by lazy { requireArguments().getLong(Key.ID) }
    private val keyViewModel: KeyViewModel by viewModel { parametersOf(id) }

    private lateinit var headerView: HeaderView
    private lateinit var scrollView: NestedScrollView
    private lateinit var exportQrView: View
    private lateinit var qrView: QRView
    private lateinit var exportTonkeeperView: View
    private lateinit var exportTonkeeperWebView: View
    private lateinit var nameView: ActionCellView
    private lateinit var hexAddressView: ActionCellView
    private lateinit var phraseView: View
    private lateinit var deleteView: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.doOnCloseClick = { finish() }

        exportQrView = view.findViewById(R.id.export_qr)
        exportQrView.background = ListCell.Position.SINGLE.drawable(requireContext())
        exportQrView.round(requireContext().getDimensionPixelSize(uikit.R.dimen.cornerMedium))

        qrView = view.findViewById(R.id.qr)

        exportTonkeeperView = view.findViewById(R.id.export_tonkeeper)
        exportTonkeeperWebView = view.findViewById(R.id.export_tonkeeper_web)

        nameView = view.findViewById(R.id.name)
        nameView.background = ListCell.Position.FIRST.drawable(requireContext())
        nameView.setOnClickListener { openNameEditor() }

        hexAddressView = view.findViewById(R.id.hex)
        hexAddressView.background = ListCell.Position.MIDDLE.drawable(requireContext())

        phraseView = view.findViewById(R.id.phrase)
        phraseView.background = ListCell.Position.LAST.drawable(requireContext())
        phraseView.setOnClickListener { openRecoveryPhrase() }

        deleteView = view.findViewById(R.id.delete_key)
        deleteView.setOnClickListener { openDeleteDialog() }

        scrollView = view.findViewById(R.id.scroll)
        scrollView.applyNavBottomPadding()

        collectFlow(keyViewModel.keyEntity, ::applyEntity)
        collectFlow(scrollView.topScrolled, headerView::setDivider)
    }

    private fun applyEntity(key: KeyEntity) {
        setName(key.name)
        setHex(key.hex)
        setExportByUri(key.publicKey, key.name)
    }

    private fun setName(name: String) {
        headerView.title = name
        nameView.subtitle = name
    }

    private fun setHex(hex: String) {
        hexAddressView.subtitle = hex.short8
        hexAddressView.setOnClickListener {
            requireContext().copyToClipboard(hex)
        }
    }

    private fun setExportByUri(publicKey: PublicKeyEd25519, name: String) {
        qrView.setContent(TKDeepLink.buildLinkUri(publicKey, name, false).toString())

        exportTonkeeperView.setOnClickListener {
            TKDeepLink.openOrInstall(requireContext(), TKDeepLink.buildLinkUri(publicKey, name, true))
        }

        exportTonkeeperWebView.setOnClickListener {
            navigation?.openURL(TKDeepLink.buildLinkUriWeb(publicKey, name).toString(), true)
        }
    }

    private fun openNameEditor() {
        navigation?.add(NameFragment.newInstance(id))
    }

    private fun openRecoveryPhrase() {
        keyViewModel.getRecoveryPhrase(requireContext()).catch {
            requireContext().authorizationRequiredError()
        }.onEach(::openRecoveryPhrase).launchIn(lifecycleScope)
    }

    private fun openRecoveryPhrase(mnemonic: Array<String>) {
        navigation?.add(PhraseFragment.newInstance(mnemonic))
    }

    private fun openDeleteDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setColoredButtons()
        builder.setTitle(R.string.delete_key_question)
        builder.setMessage(R.string.delete_key_question_subtitle)
        builder.setNegativeButton(R.string.delete_key) { _ -> delete() }
        builder.setPositiveButton(R.string.cancel)
        builder.show()
    }

    private fun delete() {
        keyViewModel.delete(requireContext()).catch {
            requireContext().authorizationRequiredError()
        }.onEach {
            finish()
        }.launchIn(lifecycleScope)
    }

}