package com.tonapps.tonkeeper.fragment.signer

import com.tonapps.tonkeeper.extensions.getSeqno
import com.tonapps.tonkeeper.fragment.send.TransactionData
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import org.ton.bitstring.BitString
import org.ton.block.StateInit
import org.ton.cell.Cell

class TransactionDataHelper(
    private val api: API
) {
    private var lastSeqno = -1
    private var lastUnsignedBody: Cell? = null

    suspend fun getStateInitIfNeed(walletLegacy: WalletLegacy): StateInit? {
        if (lastSeqno == -1) {
            lastSeqno = getSeqno(walletLegacy)
        }
        if (lastSeqno == 0) {
            return walletLegacy.contract.stateInit
        }
        return null
    }

    private suspend fun getSeqno(walletLegacy: WalletLegacy): Int {
        if (lastSeqno == 0) {
            lastSeqno = walletLegacy.getSeqno(api)
        }
        return lastSeqno
    }

    private suspend fun buildUnsignedBody(
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

    fun createTransferMessageCell(
        walletLegacy: WalletLegacy,
        signature: ByteArray
    ): Cell {
        val contract = walletLegacy.contract

        val unsignedBody = lastUnsignedBody
            ?: throw Exception("unsigned body is null")
        val signatureBitString = BitString(signature)
        val signerBody = contract.signedBody(signatureBitString, unsignedBody)
        return contract.createTransferMessageCell(
            walletLegacy.contract.address,
            lastSeqno,
            signerBody
        )
    }
}