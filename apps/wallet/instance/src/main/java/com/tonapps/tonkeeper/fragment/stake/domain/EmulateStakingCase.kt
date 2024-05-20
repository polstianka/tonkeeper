package com.tonapps.tonkeeper.fragment.stake.domain

import com.tonapps.tonkeeper.extensions.emulate
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.addStakeCellProducer
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import io.tonapi.models.MessageConsequences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class EmulateStakingCase(
    private val getStakeWalletTransferCase: CreateWalletTransferCase,
    private val api: API
) {

    suspend fun execute(
        walletLegacy: WalletLegacy,
        pool: StakingPool,
        amount: BigDecimal
    ): MessageConsequences = withContext(Dispatchers.IO) {
        val cell = pool.serviceType.addStakeCellProducer.produce()
        val walletTransfer = getStakeWalletTransferCase.execute(
            walletLegacy,
            pool.address,
            amount,
            cell
        )
        walletLegacy.emulate(api, walletTransfer)
    }
}