package com.tonapps.tonkeeper.ui.screen.stake.confirm

import androidx.lifecycle.ViewModel
import com.tonapps.blockchain.ton.TonNetwork
import com.tonapps.tonkeeper.sign.SignRequestEntity
import com.tonapps.wallet.data.account.legacy.WalletManager
import kotlinx.datetime.Clock
import org.ton.contract.wallet.WalletTransfer
import kotlin.time.Duration.Companion.seconds

class StakeConfirmationViewModel(
    private val walletManager: WalletManager
) : ViewModel() {

    suspend fun getSignRequestEntity(transfer: WalletTransfer): SignRequestEntity {
        val userWallet = walletManager.getWalletInfo()!!
        return SignRequestEntity(
            fromValue = "",
            validUntil = (Clock.System.now() + 60.seconds).epochSeconds,
            messages = emptyList(),
            network = if (userWallet.testnet) TonNetwork.TESTNET else TonNetwork.MAINNET
        ).apply { transfers = listOf(transfer) }
    }
}