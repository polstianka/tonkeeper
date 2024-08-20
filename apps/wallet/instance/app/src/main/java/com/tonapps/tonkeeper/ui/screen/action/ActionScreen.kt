package com.tonapps.tonkeeper.ui.screen.action

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tonapps.extensions.withMinus
import com.tonapps.extensions.withPlus
import com.tonapps.icu.CurrencyFormatter.withCustomSymbol
import com.tonapps.tonkeeper.api.getNameOrAddress
import com.tonapps.tonkeeper.core.history.ActionType
import com.tonapps.tonkeeper.core.history.HistoryHelper
import com.tonapps.tonkeeper.core.history.list.HistoryAdapter
import com.tonapps.tonkeeper.core.history.list.item.HistoryItem
import com.tonapps.tonkeeper.extensions.getTitle
import com.tonapps.tonkeeper.ui.base.BaseWalletScreen
import com.tonapps.tonkeeper.view.TransactionDetailView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.core.entity.SignRequestEntity
import com.tonapps.wallet.localization.Localization
import io.tonapi.models.Action
import io.tonapi.models.JettonVerificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import uikit.base.BaseFragment
import uikit.extensions.collectFlow
import uikit.widget.ProcessTaskView
import uikit.widget.SimpleRecyclerView
import uikit.widget.SlideActionView

class ActionScreen: BaseWalletScreen(R.layout.fragment_action), BaseFragment.Modal {

    private val args: ActionArgs by lazy {
        ActionArgs(requireArguments())
    }

    override val viewModel: ActionViewModel by viewModel { parametersOf(args) }
    private val adapter = HistoryAdapter()

    private lateinit var walletView: AppCompatTextView
    private lateinit var closeView: View
    private lateinit var actionsView: SimpleRecyclerView
    private lateinit var buttonsView: View
    private lateinit var confirmButton: Button
    private lateinit var cancelButton: Button
    private lateinit var processView: ProcessTaskView
    private lateinit var slideView: SlideActionView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        walletView = view.findViewById(R.id.action_wallet)

        closeView = view.findViewById(R.id.action_close)
        closeView.setOnClickListener { finish() }

        actionsView = view.findViewById(R.id.actions)
        actionsView.adapter = adapter

        adapter.submitList(args.historyItems)

        val builder = SpannableStringBuilder("≈ ")
        builder.append(args.feeFormat.withCustomSymbol(requireContext()))
        builder.append(" · ")
        builder.append(args.feeFiatFormat.withCustomSymbol(requireContext()))

        processView = view.findViewById(R.id.process)

        slideView = view.findViewById(R.id.slide)
        slideView.doOnDone = { confirm() }

        collectFlow(viewModel.walletFlow, ::applyWallet)
    }

    private fun confirm() {
        slideView.visibility = View.GONE
        processView.visibility = View.VISIBLE
        processView.state = ProcessTaskView.State.LOADING
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        viewModel.sign(requireContext()).catch {
            setFailed()
        }.onEach(::setSuccess).launchIn(lifecycleScope)
    }

    private suspend fun setFailed() = withContext(Dispatchers.Main) {
        processView.state = ProcessTaskView.State.FAILED
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        delay(2500)
        finish()
    }

    private suspend fun setSuccess(boc: String) = withContext(Dispatchers.Main) {
        processView.state = ProcessTaskView.State.SUCCESS
        navigation?.setFragmentResult(args.resultKey, Bundle().apply {
            putString(BOC_KEY, boc)
        })
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        delay(2500)
        super.finish()
    }

    override fun finish() {
        super.finish()
        navigation?.setFragmentResult(args.resultKey)
    }

    private fun applyWallet(wallet: WalletEntity) {
        val builder = SpannableStringBuilder(getString(Localization.wallet))
        builder.append(": ")
        builder.append(wallet.label.getTitle(requireContext(), walletView, 16))
        walletView.text = builder
    }

    companion object {

        private const val BOC_KEY = "boc"

        fun parseResult(bundle: Bundle): String? {
            return bundle.getString(BOC_KEY)
        }

        fun newInstance(
            details: HistoryHelper.Details,
            walletId: String,
            request: SignRequestEntity,
            requestKey: String,
            isBattery: Boolean = false,
        ) = newInstance(
            accountId = details.accountId,
            walletId = walletId,
            request = request,
            historyItems = details.items,
            feeFormat = details.feeFormat,
            feeFiatFormat = details.feeFiatFormat,
            requestKey = requestKey,
            isBattery = isBattery,
        )

        fun newInstance(
            accountId: String,
            walletId: String,
            request: SignRequestEntity,
            historyItems: List<HistoryItem>,
            feeFormat: CharSequence,
            feeFiatFormat: CharSequence,
            requestKey: String,
            isBattery: Boolean = false,
        ): ActionScreen {
            val args = ActionArgs(accountId, walletId, request, historyItems, feeFormat, feeFiatFormat, requestKey, isBattery)
            val screen = ActionScreen()
            screen.setArgs(args)
            return screen
        }
    }
}