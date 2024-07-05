package com.tonapps.signer.screen.sign

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import com.tonapps.security.hex
import com.tonapps.signer.Key
import com.tonapps.signer.R
import com.tonapps.signer.deeplink.TKDeepLink
import com.tonapps.signer.core.entities.KeyEntity
import com.tonapps.signer.deeplink.entities.ReturnResultEntity
import com.tonapps.signer.extensions.authorizationRequiredError
import com.tonapps.signer.extensions.copyToClipboard
import com.tonapps.signer.extensions.short4
import com.tonapps.signer.extensions.toast
import com.tonapps.signer.screen.emulate.EmulateFragment
import com.tonapps.signer.screen.qr.QRFragment
import com.tonapps.signer.screen.root.RootViewModel
import com.tonapps.signer.screen.sign.list.SignAdapter
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.uikit.color.textTertiaryColor
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.ton.cell.Cell
import uikit.HapticHelper
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.extensions.setColor
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.LoaderView
import uikit.widget.ModalView
import uikit.widget.SimpleRecyclerView
import uikit.widget.SlideActionView

class SignFragment: BaseFragment(R.layout.fragment_sign), BaseFragment.Modal {

    companion object {

        fun newInstance(
            id: Long,
            body: Cell,
            v: String,
            returnResult: ReturnResultEntity
        ): SignFragment {
            val fragment = SignFragment()
            fragment.arguments = SignArgs.bundle(id, body, v, returnResult)
            return fragment
        }
    }

    private val args: SignArgs by lazy { SignArgs(requireArguments()) }
    private val signViewModel: SignViewModel by viewModel { parametersOf(args.id, args.body, args.v) }
    private val rootViewModel: RootViewModel by activityViewModel()

    private val adapter = SignAdapter()

    private lateinit var closeView: View
    private lateinit var subtitleView: AppCompatTextView
    private lateinit var listView: SimpleRecyclerView
    private lateinit var auditView: FrameLayout
    private lateinit var showAuditView: View
    private lateinit var rawView: View
    private lateinit var bocRawView: AppCompatTextView
    private lateinit var emulateButton: View
    private lateinit var copyButton: View
    private lateinit var actionView: View
    private lateinit var slideView: SlideActionView
    private lateinit var processLoader: LoaderView
    private lateinit var qrView: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        closeView = view.findViewById(R.id.close)
        closeView.setOnClickListener { finish() }

        subtitleView = view.findViewById(R.id.subtitle)

        listView = view.findViewById(R.id.list)
        listView.adapter = adapter

        auditView = view.findViewById(R.id.audit)

        showAuditView = view.findViewById(R.id.show_audit)
        showAuditView.setOnClickListener { showDetails() }

        rawView = view.findViewById(R.id.raw)

        bocRawView = view.findViewById(R.id.boc_raw)
        bocRawView.text = args.bodyHex
        bocRawView.movementMethod = ScrollingMovementMethod()
        bocRawView.setOnLongClickListener {
            copyBody()
            true
        }

        emulateButton = view.findViewById(R.id.emulate)
        emulateButton.setOnClickListener { emulateBody(false) }

        copyButton = view.findViewById(R.id.copy)
        copyButton.setOnClickListener { copyBody() }

        qrView = view.findViewById(R.id.qr)
        qrView.setOnClickListener { emulateBody(true) }

        actionView = view.findViewById(R.id.action)

        slideView = view.findViewById(R.id.slide)
        slideView.doOnDone = { sign() }

        processLoader = view.findViewById(R.id.process_loader)

        collectFlow(signViewModel.actionsFlow, adapter::submitList)
        collectFlow(signViewModel.keyEntity, ::setKeyEntity)
        collectFlow(signViewModel.openDetails) { showDetails() }
    }

    private fun showError() {
        requireContext().authorizationRequiredError()
        HapticHelper.error(requireContext())
    }

    private fun copyBody() {
        requireContext().copyToClipboard(args.bodyHex)
    }

    private fun emulateBody(qr: Boolean) {
        signViewModel.openEmulate().catch {
            navigation?.toast(R.string.unknown_error)
        }.onEach{ openEmulate(it, qr) }.launchIn(lifecycleScope)
    }

    private fun openEmulate(body: String, qr: Boolean) {
        val uri = Uri.Builder().scheme("https")
            .authority("tonviewer.com")
            .appendPath("emulate")
            .appendPath(body)
            .build()
        if (qr) {
            navigation?.add(EmulateFragment.newInstance(uri.toString()))
        } else {
            navigation?.openURL(uri.toString(), true)
        }
    }

    private fun sendSignature(signature: ByteArray) {
        val returnResult = args.returnResult
        if (returnResult.toApp) {
            rootViewModel.responseSignature(signature)
        } else if (returnResult.uri != null) {
            returnSignature(returnResult.uri, signature)
        } else {
            showQR(signature)
        }
    }

    private fun returnSignature(uri: Uri, signature: ByteArray) {
        val intent = Intent(Intent.ACTION_VIEW, uri.buildUpon().appendQueryParameter(Key.SIGN, hex(signature)).build())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun showQR(signature: ByteArray) {
        val uri = TKDeepLink.buildPublishUri(signature)
        navigation?.add(QRFragment.newInstance(args.id, uri.toString()))
        finishDelay()
    }

    private fun finishDelay() {
        postDelayed(300) {
            finish()
        }
    }

    private fun sign() {
        applyLoading()
        signViewModel.sign(requireContext()).catch {
            applyDefault()
        }.onEach(::sendSignature).launchIn(lifecycleScope)
    }

    private fun showDetails() {
        showAuditView.visibility = View.GONE
        rawView.visibility = View.VISIBLE
    }

    private fun applyLoading() {
        slideView.visibility = View.GONE
        processLoader.visibility = View.VISIBLE
    }

    private fun applyDefault() {
        slideView.visibility = View.VISIBLE
        slideView.reset()

        processLoader.visibility = View.GONE
        showError()
    }

    private fun setKeyEntity(entity: KeyEntity) {
        setSubtitle(entity.name, entity.hex.short4)
    }

    private fun setSubtitle(label: String, hex: String) {
        val span = SpannableString("$label $hex")
        span.setColor(requireContext().textTertiaryColor,0, label.length)
        span.setColor(requireContext().textSecondaryColor, label.length, span.length)
        subtitleView.text = span
    }
}