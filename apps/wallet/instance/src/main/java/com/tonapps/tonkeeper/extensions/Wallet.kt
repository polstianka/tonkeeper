package com.tonapps.tonkeeper.extensions

import com.tonapps.blockchain.ton.extensions.EmptyPrivateKeyEd25519
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.entities.WalletLabel
import io.tonapi.models.MessageConsequences
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.cell.Cell
import org.ton.contract.wallet.WalletTransfer
import ton.extensions.base64
import com.tonapps.wallet.data.account.legacy.WalletLegacy

val WalletLegacy.label: WalletLabel
    get() = WalletLabel(
        accountName = name,
        emoji = emoji,
        color = color,
    )

suspend fun WalletLegacy.buildBody(
    validUntil: Long,
    api: API,
    vararg gifts: WalletTransfer
): Pair<Cell, Int> {
    val seqno = getSeqno(api)
    val cell = contract.createTransferUnsignedBody(seqno = seqno, gifts = gifts, validUntil = validUntil)
    return Pair(cell, seqno)
}

suspend fun WalletLegacy.sign(
    validUntil: Long,
    api: API,
    privateKey: PrivateKeyEd25519 = EmptyPrivateKeyEd25519,
    vararg gifts: WalletTransfer
): Cell {
    val (unsignedBody, seqno) = buildBody(validUntil, api, *gifts)
    return sign(
        seqno = seqno,
        privateKey = privateKey,
        unsignedBody = unsignedBody,
    )
}

suspend fun WalletLegacy.emulate(
    api: API,
    boc: String
): MessageConsequences {
    return api.emulate(boc, testnet)
}

suspend fun WalletLegacy.emulate(
    api: API,
    cell: Cell
) = emulate(api, cell.base64())

suspend fun WalletLegacy.emulate(
    validUntil: Long,
    api: API,
    vararg gifts: WalletTransfer
): MessageConsequences {
    val cell = sign(validUntil, api, gifts = gifts)
    return emulate(api, cell)
}

suspend fun WalletLegacy.emulate(
    validUntil: Long,
    api: API,
    gifts: List<WalletTransfer>
): MessageConsequences {
    val cell = sign(validUntil, api, gifts = gifts.toTypedArray())
    return emulate(api, cell)
}

suspend fun WalletLegacy.sendToBlockchain(
    api: API,
    boc: String
): Boolean {
    return api.sendToBlockchain(boc, testnet)
}

suspend fun WalletLegacy.sendToBlockchain(
    api: API,
    cell: Cell
) = sendToBlockchain(api, cell.base64())

suspend fun WalletLegacy.sendToBlockchain(
    validUntil: Long,
    api: API,
    privateKey: PrivateKeyEd25519,
    vararg gifts: WalletTransfer
): Cell? {
    val cell = sign(validUntil, api, privateKey = privateKey, gifts = gifts)
    if (sendToBlockchain(api, cell)) {
        return cell
    }
    return null
}

suspend fun WalletLegacy.sendToBlockchain(
    validUntil: Long,
    api: API,
    privateKey: PrivateKeyEd25519,
    gifts: List<WalletTransfer>
): Cell? {
    return sendToBlockchain(validUntil, api, privateKey = privateKey, gifts = gifts.toTypedArray())
}

suspend fun WalletLegacy.getSeqno(api: API): Int {
    return try {
        api.getAccountSeqno(accountId, testnet)
    } catch (e: Throwable) {
        0
    }
}
