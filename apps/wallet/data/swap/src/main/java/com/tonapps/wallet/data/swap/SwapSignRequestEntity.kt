package com.tonapps.wallet.data.swap

import com.tonapps.blockchain.ton.TonNetwork
import org.ton.contract.wallet.WalletTransfer

data class SwapSignRequestEntity(
    val fromValue: String?,
    val validUntil: Long,
    val walletTransfer: WalletTransfer,
    val network: TonNetwork
)

