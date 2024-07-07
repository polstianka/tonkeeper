package com.tonapps.wallet.data.account

import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import org.ton.block.StateInit

object SeqnoHelper {
    private var lastSeqno = -1

    suspend fun getStateInitIfNeed(wallet: WalletLegacy, api: API): StateInit? {
        if (lastSeqno == -1) {
            lastSeqno = getSeqno(wallet, api)
        }
        if (lastSeqno == 0) {
            return wallet.contract.stateInit
        }
        return null
    }

    private suspend fun getSeqno(wallet: WalletLegacy, api: API): Int {
        if (lastSeqno == 0) {
            lastSeqno = wallet.getSeqnoRemote(api)
        }
        return lastSeqno
    }

    private suspend fun WalletLegacy.getSeqnoRemote(api: API): Int {
        return try {
            api.getAccountSeqno(accountId, testnet)
        } catch (e: Throwable) {
            0
        }
    }
}