package com.tonapps.tonkeeper.ui.screen.ledger.proof

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.tonapps.blockchain.ton.extensions.toByteArray
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.ui.screen.ledger.sign.LedgerSignScreen.Companion.SIGNED_MESSAGE
import com.tonapps.tonkeeper.ui.screen.ledger.steps.LedgerConnectionFragment
import com.tonapps.tonkeeper.ui.screen.ledger.steps.LedgerConnectionViewModel
import com.tonapps.tonkeeper.ui.screen.ledger.steps.LedgerEvent
import com.tonapps.tonkeeper.ui.screen.ledger.update.LedgerUpdateScreen
import com.tonapps.tonkeeperx.R
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.collectFlow
import uikit.extensions.getDimensionPixelSize
import uikit.navigation.Navigation.Companion.navigation
import java.math.BigInteger

class LedgerProofScreen : BaseFragment(R.layout.fragment_ledger_sign), BaseFragment.Modal {

    private val args: LedgerProofArgs by lazy { LedgerProofArgs(requireArguments()) }

    private val connectionViewModel: LedgerConnectionViewModel by viewModel()

    private val ledgerConnectionFragment: LedgerConnectionFragment by lazy {
        LedgerConnectionFragment.newInstance()
    }

    private var isSuccessful: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectionViewModel.setProofData(args.domain, args.timestamp, args.payload, args.walletId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.container)
            .applyNavBottomPadding(requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium))

        view.findViewById<View>(R.id.close).setOnClickListener { finish() }
        view.findViewById<View>(R.id.cancel).setOnClickListener { finish() }

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction().replace(R.id.steps, ledgerConnectionFragment)
                .commit()
        }

        collectFlow(connectionViewModel.eventFlow, ::onEvent)
    }

    /*override fun onDestroy() {
        super.onDestroy()
        if (!requireActivity().isChangingConfigurations && !isSuccessful) {
            navigation?.setFragmentResult(args.requestKey, Bundle())
        }
    }*/

    private fun onEvent(event: LedgerEvent) {
        when (event) {
            is LedgerEvent.Ready -> {
                if (event.isReady) {
                    lifecycleScope.launch { connectionViewModel.signDomainProof() }
                }
            }

            is LedgerEvent.WrongVersion -> {
                onWrongVersion(event.requiredVersion)
            }

            is LedgerEvent.Error -> {
                navigation?.toast(event.message)
                finish()
            }

            is LedgerEvent.SignedProof -> {
                onSuccess(event.proof)
            }

            is LedgerEvent.Rejected -> {

                finish()
            }

            else -> null
        }
    }

    private fun onWrongVersion(requiredVersion: String) {
        navigation?.add(LedgerUpdateScreen.newInstance(requiredVersion))
        finish()
    }

    private fun onSuccess(proof: ByteArray) {
        val bundle = Bundle().apply {
            putByteArray(SIGNED_PROOF, proof)
        }
        setResult(bundle)
        isSuccessful = true
    }

    companion object {
        const val SIGNED_PROOF = "signed_proof"

        fun newInstance(
            domain: String,
            timestamp: BigInteger,
            payload: String,
            walletId: String
        ): LedgerProofScreen {
            return LedgerProofScreen().apply {
                setArgs(LedgerProofArgs(domain, timestamp, payload, walletId))
            }
        }
    }
}