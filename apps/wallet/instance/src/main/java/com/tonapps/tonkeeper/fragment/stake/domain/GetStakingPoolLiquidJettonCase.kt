package com.tonapps.tonkeeper.fragment.stake.domain

import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPoolLiquidJetton
import com.tonapps.wallet.data.account.legacy.WalletManager
import com.tonapps.wallet.data.core.WalletCurrency

class GetStakingPoolLiquidJettonCase(
    private val walletManager: WalletManager,
    private val repository: StakingRepository
) {

    suspend fun execute(
        pool: StakingPool,
        currency: WalletCurrency
    ): StakingPoolLiquidJetton? {
        val testnet = walletManager.getWalletInfo()?.testnet == true
        return if (pool.liquidJettonMaster == null) {
            null
        } else {
            repository.getJetton(pool.liquidJettonMaster, pool.name, currency, testnet)
        }
    }
}