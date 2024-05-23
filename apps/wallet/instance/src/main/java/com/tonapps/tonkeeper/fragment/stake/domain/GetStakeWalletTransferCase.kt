package com.tonapps.tonkeeper.fragment.stake.domain

import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.getAmount
import com.tonapps.tonkeeper.fragment.stake.domain.model.getCellProducer
import com.tonapps.tonkeeper.fragment.stake.domain.model.getDestinationAddress
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import org.ton.contract.wallet.WalletTransfer
import java.math.BigDecimal

class GetStakeWalletTransferCase(
    private val createWalletTransferCase: CreateWalletTransferCase
) {

    suspend fun execute(
        pool: StakingPool,
        direction: StakingTransactionType,
        amount: BigDecimal,
        wallet: WalletLegacy,
        isSendAll: Boolean
    ): WalletTransfer {
        val cell = pool.serviceType.getCellProducer(
            direction,
            amount,
            wallet.contract.address,
            isSendAll
        ).produce()
        val address = pool.getDestinationAddress(direction)
        val toSendAmount = pool.serviceType.getAmount(direction, amount, isSendAll)
        return createWalletTransferCase.execute(
            wallet,
            address,
            toSendAmount,
            cell
        )
    }
}