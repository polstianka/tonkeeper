package com.tonapps.wallet.data.account.entities

import com.tonapps.blockchain.ton.contract.BaseWalletContract
import com.tonapps.blockchain.ton.contract.WalletV3R1Contract
import com.tonapps.blockchain.ton.contract.WalletV3R2Contract
import com.tonapps.blockchain.ton.contract.WalletV4R1Contract
import com.tonapps.blockchain.ton.contract.WalletV4R2Contract
import com.tonapps.blockchain.ton.contract.WalletVersion
import com.tonapps.blockchain.ton.extensions.EmptyPrivateKeyEd25519
import com.tonapps.blockchain.ton.extensions.toAccountId
import com.tonapps.blockchain.ton.extensions.toWalletAddress
import com.tonapps.wallet.data.account.WalletSource
import com.tonapps.wallet.data.account.WalletType
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.api.pub.PublicKeyEd25519
import org.ton.block.AddrStd
import org.ton.block.MsgAddressInt
import org.ton.cell.Cell
import org.ton.contract.wallet.WalletTransfer

data class WalletEntity(
    val id: Long,
    val publicKey: PublicKeyEd25519,
    val type: WalletType,
    val version: WalletVersion = WalletVersion.V4R2,
    val label: WalletLabel,
    val source: WalletSource = WalletSource.Default
) {

    companion object {
        const val WORKCHAIN = 0
    }

    constructor(legacy: WalletLegacy) : this(
        id = legacy.id,
        publicKey = legacy.publicKey,
        type = legacy.type,
        version = legacy.version,
        label = WalletLabel(
            accountName = legacy.name,
            emoji = legacy.emoji.toString(),
            color = legacy.color
        ),
        source = legacy.source
    )

    val contract: BaseWalletContract = when (version) {
        WalletVersion.V4R2 -> WalletV4R2Contract(WORKCHAIN, publicKey)
        WalletVersion.V3R2 -> WalletV3R2Contract(WORKCHAIN, publicKey)
        WalletVersion.V3R1 -> WalletV3R1Contract(WORKCHAIN, publicKey)
        WalletVersion.V4R1 -> WalletV4R1Contract(WORKCHAIN, publicKey)
        else -> throw IllegalArgumentException("Unsupported wallet version: $version")
    }

    val testnet: Boolean
        get() = type == WalletType.Testnet

    val hasPrivateKey: Boolean
        get() = type == WalletType.Default || type == WalletType.Testnet

    val accountId: String = contract.address.toAccountId()

    val address: String = contract.address.toWalletAddress(testnet)

    fun createBody(
        seqno: Int,
        validUntil: Long,
        gifts: List<WalletTransfer>
    ): Cell {
        return contract.createTransferUnsignedBody(
            validUntil = validUntil,
            seqno = seqno,
            gifts = gifts.toTypedArray()
        )
    }

    fun sign(
        privateKeyEd25519: PrivateKeyEd25519,
        seqno: Int,
        body: Cell
    ): Cell {
        return contract.createTransferMessageCell(
            address = contract.address,
            privateKey = privateKeyEd25519,
            seqno = seqno,
            unsignedBody = body,
        )
    }
}