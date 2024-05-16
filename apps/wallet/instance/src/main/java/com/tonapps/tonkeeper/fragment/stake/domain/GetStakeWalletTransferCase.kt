package com.tonapps.tonkeeper.fragment.stake.domain

import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.extensions.getSeqno
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import org.ton.block.AddrStd
import org.ton.block.Coins
import org.ton.cell.Cell
import org.ton.contract.wallet.WalletTransfer
import org.ton.contract.wallet.WalletTransferBuilder
import ton.SendMode

class GetStakeWalletTransferCase(
    private val api: API,
    private val getStateInitCase: GetStateInitCase
) {

    suspend fun getWalletTransfer(
        wallet: WalletLegacy,
        pool: StakingPool,
        amount: Float,
        bodyCell: Cell
    ): WalletTransfer {
        val seqno = wallet.getSeqno(api)
        val stateInit = getStateInitCase.execute(seqno, wallet)
        return WalletTransferBuilder().apply {
            bounceable = false
            destination = AddrStd.parse(pool.address)
            body = bodyCell
            sendMode = SendMode.PAY_GAS_SEPARATELY.value + SendMode.IGNORE_ERRORS.value
            coins = Coins.ofNano(Coin.toNano(amount))
            this.stateInit = stateInit
        }.build()
    }
}