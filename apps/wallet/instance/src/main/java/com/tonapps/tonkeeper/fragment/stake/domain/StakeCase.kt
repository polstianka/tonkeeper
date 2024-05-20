package com.tonapps.tonkeeper.fragment.stake.domain

import com.tonapps.tonkeeper.extensions.sendToBlockchain
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.addStakeCellProducer
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.account.legacy.WalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class StakeCase(
    private val api: API,
    private val walletManager: WalletManager,
    private val getStakeWalletTransferCase: CreateWalletTransferCase
) {
    suspend fun execute(
        wallet: WalletLegacy,
        pool: StakingPool,
        amount: BigDecimal
    ): Boolean = withContext(Dispatchers.IO) {
        val cell = pool.serviceType.addStakeCellProducer.produce()
        val walletTransfer = getStakeWalletTransferCase.execute(
            wallet,
            pool.address,
            amount,
            cell
        )
        val privateKey = walletManager.getPrivateKey(wallet.id)
        val result = wallet.sendToBlockchain(api, privateKey, walletTransfer)
        result != null
    }
}