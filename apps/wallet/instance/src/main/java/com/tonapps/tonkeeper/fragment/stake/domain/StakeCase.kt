package com.tonapps.tonkeeper.fragment.stake.domain

import com.tonapps.tonkeeper.extensions.sendToBlockchain
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.account.legacy.WalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class StakeCase(
    private val api: API,
    private val walletManager: WalletManager,
    private val getStakeWalletTransferCase: GetStakeWalletTransferCase
) {
    suspend fun execute(
        wallet: WalletLegacy,
        pool: StakingPool,
        amount: BigDecimal,
        direction: StakingTransactionType,
        isSendAll: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        val walletTransfer = getStakeWalletTransferCase.execute(
            pool,
            direction,
            amount,
            wallet,
            isSendAll
        )
        val privateKey = walletManager.getPrivateKey(wallet.id)
        val result = wallet.sendToBlockchain(api, privateKey, walletTransfer)
        result != null
    }
}