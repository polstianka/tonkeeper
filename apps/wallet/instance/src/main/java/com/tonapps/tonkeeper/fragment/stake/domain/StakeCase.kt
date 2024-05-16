package com.tonapps.tonkeeper.fragment.stake.domain

import com.tonapps.blockchain.Coin
import com.tonapps.tonkeeper.extensions.getSeqno
import com.tonapps.tonkeeper.extensions.sendToBlockchain
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.addStakeCellProducer
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.account.legacy.WalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ton.block.AddrStd
import org.ton.block.Coins
import org.ton.block.StateInit
import org.ton.contract.wallet.WalletTransferBuilder
import ton.SendMode

class StakeCase(
    private val api: API,
    private val walletManager: WalletManager
) {
    suspend fun execute(
        wallet: WalletLegacy,
        pool: StakingPool,
        amount: Float
    ): Boolean = withContext(Dispatchers.IO) {
        val seqno = wallet.getSeqno(api)
        val stateInit = getStateInit(seqno, wallet)
        val walletTransfer = getWalletTransfer(pool, amount, stateInit)
        val privateKey = walletManager.getPrivateKey(wallet.id)
        val result = wallet.sendToBlockchain(api, privateKey, walletTransfer)
        result != null
    }

    private fun getWalletTransfer(
        pool: StakingPool,
        amount: Float,
        stateInit: StateInit?
    ) = WalletTransferBuilder().apply {
        bounceable = false
        destination = AddrStd.parse(pool.address)
        body = pool.serviceType.addStakeCellProducer.produce()
        sendMode = SendMode.PAY_GAS_SEPARATELY.value + SendMode.IGNORE_ERRORS.value
        coins = Coins.ofNano(Coin.toNano(amount))
        this.stateInit = stateInit
    }.build()

    private fun getStateInit(
        seqno: Int,
        wallet: WalletLegacy
    ) = if (seqno == 0) {
        wallet.contract.stateInit
    } else {
        null
    }
}