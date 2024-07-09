package com.tonapps.tonkeeper.fragment.stake.domain

import com.tonapps.tonkeeper.extensions.emulate
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import io.tonapi.models.MessageConsequences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class EmulateStakingCase(
    private val api: API,
    private val getStakeWalletTransferCase: GetStakeWalletTransferCase
) {

    suspend fun execute(
        walletLegacy: WalletLegacy,
        pool: StakingPool,
        amount: BigDecimal,
        type: StakingTransactionType,
        isSendAll: Boolean
    ): MessageConsequences = withContext(Dispatchers.IO) {
        val walletTransfer = getStakeWalletTransferCase.execute(
            pool,
            type,
            amount,
            walletLegacy,
            isSendAll
        )
        walletLegacy.emulate(api, walletTransfer)
    }
}