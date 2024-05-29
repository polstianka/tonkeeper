package com.tonapps.tonkeeper.ui.screen.stake.confirm


import com.tonapps.tonkeeper.core.history.list.item.HistoryItem
import com.tonapps.wallet.data.account.entities.WalletLabel
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import io.tonapi.models.PoolInfo
import uikit.mvi.UiState
import uikit.widget.ProcessTaskView

data class StakeConfirmScreenState(
    val amount: CharSequence? = null,
    val amountInCurrency: String? = null,
    val feeValue: Long = 0,
    val fee: String? = null,
    val feeInCurrency: String? = null,
    val processActive: Boolean = false,
    val processState: ProcessTaskView.State = ProcessTaskView.State.LOADING,
    val buttonEnabled: Boolean = false,
    val emulatedEventItems: List<HistoryItem> = emptyList(),
    val signer: Boolean = false,
    val walletLabel: WalletLabel? = null,
    val poolInfo: PoolInfo? = null,
    val isUnstake: Boolean = false,
    val address: String = ""
) : UiState()