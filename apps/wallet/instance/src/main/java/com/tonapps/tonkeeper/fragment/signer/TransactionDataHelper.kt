package com.tonapps.tonkeeper.fragment.signer

import com.tonapps.tonkeeper.extensions.getSeqno
import com.tonapps.tonkeeper.fragment.send.TransactionData
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import org.ton.block.StateInit
import org.ton.cell.Cell

class TransactionDataHelper(
    private val api: API
) {
    var lastSeqno = -1
    var lastUnsignedBody: Cell? = null

    suspend fun getStateInitIfNeed(walletLegacy: WalletLegacy): StateInit? {
        if (lastSeqno == -1) {
            lastSeqno = getSeqno(walletLegacy)
        }
        if (lastSeqno == 0) {
            return walletLegacy.contract.stateInit
        }
        return null
    }

    suspend fun getSeqno(walletLegacy: WalletLegacy): Int {
        if (lastSeqno == 0) {
            lastSeqno = walletLegacy.getSeqno(api)
        }
        return lastSeqno
    }

    suspend fun buildUnsignedBody(
        wallet: WalletLegacy,
        seqno: Int,
        tx: TransactionData
    ): Cell {
        val stateInit = getStateInitIfNeed(wallet)
        val transfer = tx.buildWalletTransfer(wallet.contract.address, stateInit)
        return wallet.contract.createTransferUnsignedBody(seqno = seqno, gifts = arrayOf(transfer))
    }

    suspend fun buildSignRequest(walletLegacy: WalletLegacy, tx: TransactionData): SignRequest {
        lastSeqno = getSeqno(walletLegacy)
        val cell = buildUnsignedBody(walletLegacy, lastSeqno, tx)
        lastUnsignedBody = cell
        return SignRequest(cell, walletLegacy.publicKey)
    }
}