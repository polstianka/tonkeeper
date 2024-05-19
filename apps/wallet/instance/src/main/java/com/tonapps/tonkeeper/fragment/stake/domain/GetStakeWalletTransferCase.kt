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
import java.math.BigDecimal

class GetStakeWalletTransferCase(
    private val api: API,
    private val getStateInitCase: GetStateInitCase
) {

    suspend fun getWalletTransfer(
        wallet: WalletLegacy,
        pool: StakingPool,
        amount: BigDecimal,
        bodyCell: Cell
    ): WalletTransfer {
        val seqno = wallet.getSeqno(api)
        val stateInit = getStateInitCase.execute(seqno, wallet)
        val poolAddress = "0:d5d4b4d4d2c5a88e60d45781ee96b9583e9c4b59252333a177954bab31bc216a"
        return WalletTransferBuilder().apply {
            bounceable = false
            destination = AddrStd.parse(poolAddress)
            body = bodyCell
            sendMode = SendMode.PAY_GAS_SEPARATELY.value + SendMode.IGNORE_ERRORS.value
            coins = Coins.ofNano(Coin.toNano(amount))
            this.stateInit = stateInit
        }.build()
    }
}