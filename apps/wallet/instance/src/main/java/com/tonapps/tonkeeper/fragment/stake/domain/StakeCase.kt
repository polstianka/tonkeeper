package com.tonapps.tonkeeper.fragment.stake.domain

import com.tonapps.tonkeeper.extensions.sendToBlockchain
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingServiceType
import com.tonapps.tonkeeper.fragment.stake.domain.model.addStakeCellProducer
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.account.legacy.WalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StakeCase(
    private val api: API,
    private val walletManager: WalletManager,
    private val getStakeWalletTransferCase: GetStakeWalletTransferCase
) {
    suspend fun execute(
        wallet: WalletLegacy,
        pool: StakingPool,
        amount: Float
    ): Boolean = withContext(Dispatchers.IO) {
        val cell = StakingServiceType.WHALES.addStakeCellProducer.produce()//pool.serviceType.addStakeCellProducer.produce()
        val walletTransfer = getStakeWalletTransferCase.getWalletTransfer(
            wallet,
            pool,
            amount,
            cell
        )
        val privateKey = walletManager.getPrivateKey(wallet.id)
        val result = wallet.sendToBlockchain(api, privateKey, walletTransfer)
        result != null
    }
}