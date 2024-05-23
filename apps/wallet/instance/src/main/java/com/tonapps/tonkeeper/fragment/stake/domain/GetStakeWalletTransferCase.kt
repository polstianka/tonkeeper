package com.tonapps.tonkeeper.fragment.stake.domain

import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.getAmount
import com.tonapps.tonkeeper.fragment.stake.domain.model.getCellProducer
import com.tonapps.tonkeeper.fragment.stake.domain.model.getDestinationAddress
import com.tonapps.tonkeeper.fragment.swap.domain.JettonWalletAddressRepository
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import org.ton.block.MsgAddressInt
import org.ton.contract.wallet.WalletTransfer
import java.math.BigDecimal

class GetStakeWalletTransferCase(
    private val createWalletTransferCase: CreateWalletTransferCase,
    private val jettonWalletAddressRepository: JettonWalletAddressRepository
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
            ?: jettonWalletAddressRepository.getJettonAddress(
                pool.liquidJettonMaster!!,
                wallet.address
            ).let { MsgAddressInt.parse(it) }
        val toSendAmount = pool.serviceType.getAmount(direction, amount, isSendAll)
        return createWalletTransferCase.execute(
            wallet,
            address,
            toSendAmount,
            cell
        )
    }
}