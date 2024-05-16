package com.tonapps.tonkeeper.fragment.stake.domain

import com.tonapps.wallet.data.account.legacy.WalletLegacy
import org.ton.block.StateInit

class GetStateInitCase {
    fun execute(
        seqno: Int,
        wallet: WalletLegacy
    ): StateInit? {
        return if (seqno == 0) {
            wallet.contract.stateInit
        } else {
            null
        }
    }
}