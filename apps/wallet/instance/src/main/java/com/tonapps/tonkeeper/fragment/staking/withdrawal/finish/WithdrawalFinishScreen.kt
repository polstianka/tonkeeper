package com.tonapps.tonkeeper.fragment.staking.withdrawal.finish

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.tonapps.tonkeeper.api.icon
import com.tonapps.tonkeeper.api.totalFees
import com.tonapps.tonkeeper.fragment.staking.StakingScreen
import com.tonapps.tonkeeper.helper.flow.TransactionSender
import com.tonapps.tonkeeper.ui.component.BlurredScrollView
import com.tonapps.tonkeeper.view.TransactionDetailView
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.backgroundTransparentColor
import com.tonapps.wallet.api.entity.BalanceStakeEntity
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import uikit.base.BaseFragment
import uikit.extensions.bottomScrolled
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.extensions.findFragment
import uikit.extensions.topScrolled
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FooterViewEmpty
import uikit.widget.HeaderView
import uikit.widget.ProcessTaskView
import uikit.widget.SlideActionView
import java.math.BigInteger

class WithdrawalFinishScreen(
    private val entity: BalanceStakeEntity
) : BaseFragment(R.layout.fragment_withdrawal_finish), BaseFragment.BottomSheet {
    private val withdrawalFinishScreenViewModel: WithdrawalFinishScreenViewModel by viewModel {
        parametersOf(
            entity
        )
    }
    private val sender by lazy { TransactionSender.FragmentSenderController(this, withdrawalFinishScreenViewModel.transactionSender) }

    private lateinit var iconView: AppCompatImageView
    private lateinit var titleView: AppCompatTextView
    private lateinit var currencyView: AppCompatTextView
    private lateinit var actionTitle: AppCompatTextView

    private lateinit var walletView: TransactionDetailView
    private lateinit var recipientView: TransactionDetailView
    private lateinit var apyView: TransactionDetailView
    private lateinit var feeView: TransactionDetailView
    private lateinit var actionView: View

    private lateinit var sendButton: SlideActionView
    private lateinit var processView: ProcessTaskView

    private lateinit var headerView: HeaderView
    private lateinit var listView: BlurredScrollView
    private lateinit var footerView: FooterViewEmpty

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerView = view.findViewById(R.id.header)
        headerView.setColor(requireContext().backgroundTransparentColor)

        listView = view.findViewById(R.id.list)
        listView.blurDisabled = true
        listView.blurredPaddingTop = 64.dp
        listView.blurredPaddingBottom = 180.dp

        footerView = view.findViewById(R.id.footer)
        footerView.setColor(requireContext().backgroundTransparentColor)

        collectFlow(listView.topScrolled, headerView::setDivider)
        collectFlow(listView.bottomScrolled, footerView::setDivider)

        iconView = view.findViewById(R.id.icon)
        iconView.clipToOutline = true

        titleView = view.findViewById(R.id.title)
        currencyView = view.findViewById(R.id.value_currency)

        actionTitle = view.findViewById(R.id.action_name)
        actionTitle.setText(Localization.get_withdrawal)

        walletView = view.findViewById(R.id.wallet)
        walletView.title = getString(Localization.wallet)
        walletView.position = com.tonapps.uikit.list.ListCell.Position.FIRST
        walletView.isEnabled = false

        recipientView = view.findViewById(R.id.recipient)
        recipientView.title = getString(Localization.recipient)
        recipientView.position = com.tonapps.uikit.list.ListCell.Position.MIDDLE
        recipientView.isEnabled = false

        apyView = view.findViewById(R.id.row_apy)
        apyView.isVisible = false

        feeView = view.findViewById(R.id.fee)
        feeView.title = getString(Localization.fee)
        feeView.position = com.tonapps.uikit.list.ListCell.Position.LAST
        feeView.isEnabled = false

        actionView = view.findViewById(R.id.action)
        sendButton = view.findViewById(R.id.slide)
        sendButton.doOnDone = {
            withdrawalFinishScreenViewModel.send(requireContext())
        }
        processView = view.findViewById(R.id.process)

        collectFlow(withdrawalFinishScreenViewModel.transactionEmulationFlow) { emulation ->
            val pool = emulation.request.stake.pool

            titleView.text = emulation.request.valueFmt
            currencyView.text = emulation.request.valueCurrencyFmt

            iconView.setImageResource(pool.implementation.type.icon)
            recipientView.value = pool.name

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

        collectFlow(withdrawalFinishScreenViewModel.transactionSender.statusFlow) { state ->
            if (state.processActive) {
                sendButton.visibility = View.GONE
                processView.visibility = View.VISIBLE
                processView.state = state.processState
            } else {
                sendButton.visibility = View.VISIBLE
                processView.visibility = View.GONE
            }
        }

        sender.attach { effect ->
            if (effect.navigateToHistory) {
                activity?.supportFragmentManager?.findFragment<StakingScreen>()?.finish()
                navigation?.openURL("tonkeeper://activity")
            }

            finish()
        }
    }

    companion object {
        fun newInstance(entity: BalanceStakeEntity) = WithdrawalFinishScreen(entity)
    }
}