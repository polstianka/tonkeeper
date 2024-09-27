package com.tonapps.tonkeeper.extensions

import com.tonapps.blockchain.ton.extensions.equalsAddress
import com.tonapps.blockchain.ton.extensions.toAccountId
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.core.entity.SignRequestEntity
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ton.block.AddrStd
import org.ton.contract.wallet.WalletTransfer

suspend fun SignRequestEntity.getTransfers(
    wallet: WalletEntity,
    compressedTokens: List<AccountTokenEntity>,
    excessesAddress: AddrStd? = null,
    api: API,
): List<WalletTransfer> = withContext(Dispatchers.IO) {
    val transfers = mutableListOf<WalletTransfer>()
    for (message in messages) {
        val jetton = compressedTokens.firstOrNull {
            it.address.equalsAddress(message.addressValue) ||
            it.balance.walletAddress.equalsAddress(message.addressValue)
        }
        val jettonCustomPayload = jetton?.let {
            api.getJettonCustomPayload(wallet.accountId, wallet.testnet, it.address)
        }

        val transfer = message.getWalletTransfer(
            excessesAddress = excessesAddress,
            newStateInit = jettonCustomPayload?.stateInit,
            newCustomPayload = jettonCustomPayload?.customPayload,
        )
        transfers.add(transfer)
    }
    transfers
}