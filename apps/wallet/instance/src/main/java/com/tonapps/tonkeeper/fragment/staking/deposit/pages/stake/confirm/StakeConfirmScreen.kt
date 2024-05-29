package com.tonapps.tonkeeper.fragment.staking.deposit.pages.stake.confirm

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.tonkeeper.api.icon
import com.tonapps.tonkeeper.api.percentage
import com.tonapps.tonkeeper.api.totalFees
import com.tonapps.tonkeeper.extensions.findParent
import com.tonapps.tonkeeper.fragment.staking.deposit.DepositScreen
import com.tonapps.tonkeeper.fragment.staking.deposit.DepositScreenViewModel
import com.tonapps.tonkeeper.ui.component.BlurredScrollView
import com.tonapps.tonkeeper.view.TransactionDetailView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundTransparentColor
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.bottomScrolled
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.widget.FooterViewEmpty
import uikit.widget.ProcessTaskView
import uikit.widget.SlideActionView
import java.math.BigInteger

class StakeConfirmScreen : BaseFragment(R.layout.fragment_stake_confirm) {
    private val poolsViewModel: DepositScreenViewModel by viewModel(ownerProducer = { this.findParent<DepositScreen>() })

    private lateinit var iconView: AppCompatImageView
    private lateinit var titleView: AppCompatTextView
    private lateinit var currencyView: AppCompatTextView

    private lateinit var walletView: TransactionDetailView
    private lateinit var recipientView: TransactionDetailView
    private lateinit var apyView: TransactionDetailView
    private lateinit var feeView: TransactionDetailView
    private lateinit var actionView: View

    private lateinit var sendButton: SlideActionView
    private lateinit var processView: ProcessTaskView

    private lateinit var listView: BlurredScrollView
    private lateinit var footerView: FooterViewEmpty

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = view.findViewById(R.id.list)
        listView.blurDisabled = true
        listView.blurredPaddingTop = 64.dp
        listView.blurredPaddingBottom = 88.dp
        listView.isNestedScrollingEnabled = true

        footerView = view.findViewById(R.id.footer)
        footerView.setColor(requireContext().backgroundTransparentColor)

        collectFlow(listView.bottomScrolled, footerView::setDivider)

        iconView = view.findViewById(R.id.icon)
        iconView.clipToOutline = true

        titleView = view.findViewById(R.id.title)
        currencyView = view.findViewById(R.id.value_currency)

        walletView = view.findViewById(R.id.wallet)
        walletView.title = getString(Localization.wallet)
        walletView.position = com.tonapps.uikit.list.ListCell.Position.FIRST
        walletView.isEnabled = false

        recipientView = view.findViewById(R.id.recipient)
        recipientView.title = getString(Localization.recipient)
        recipientView.position = com.tonapps.uikit.list.ListCell.Position.MIDDLE
        recipientView.isEnabled = false

        apyView = view.findViewById(R.id.row_apy)
        apyView.title = getString(Localization.staking_apy)
        apyView.position = com.tonapps.uikit.list.ListCell.Position.MIDDLE
        apyView.isEnabled = false

        feeView = view.findViewById(R.id.fee)
        feeView.title = getString(Localization.fee)
        feeView.position = com.tonapps.uikit.list.ListCell.Position.LAST
        feeView.isEnabled = false

        actionView = view.findViewById(R.id.action)
        sendButton = view.findViewById(R.id.slide)
        sendButton.doOnDone = {
            poolsViewModel.send(requireContext())
        }
        processView = view.findViewById(R.id.process)

        collectFlow(poolsViewModel.transactionEmulationFlow) { emulation ->
            val pool = emulation.request.pool

            titleView.text = emulation.request.valueFmt
            currencyView.text = emulation.request.valueCurrencyFmt

            iconView.setImageResource(pool.implementation.type.icon)
            recipientView.value = pool.name
            apyView.value = "≈ " + pool.apy.percentage

            walletView.value = emulation.request.walletEntity.label.title

            if (emulation.loading) {
                feeView.setLoading()
            } else {
                emulation.result?.let { result ->
                    val totalFee = BigInteger.valueOf(result.totalFees)
                    feeView.setData(
                        emulation.request.getFeeFmt(totalFee),
                        emulation.request.getFeeInCurrencyFmt(totalFee)
                    )
                } ?: run {
                    feeView.setData("Error", null)
                }
            }
        }

        collectFlow(poolsViewModel.transactionSender.statusFlow) { state ->
            if (state.processActive) {
                sendButton.visibility = View.GONE
                processView.visibility = View.VISIBLE
                processView.state = state.processState
            } else {
                sendButton.visibility = View.VISIBLE
                processView.visibility = View.GONE
            }
        }
    }

    companion object {
        fun newInstance() = StakeConfirmScreen()
    }
}