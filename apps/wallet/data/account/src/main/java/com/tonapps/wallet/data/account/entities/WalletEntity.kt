package com.tonapps.wallet.data.account.entities

import android.os.Parcelable
import android.util.Log
import com.tonapps.blockchain.ton.TonNetwork
import com.tonapps.blockchain.ton.contract.BaseWalletContract
import com.tonapps.blockchain.ton.contract.WalletFeature
import com.tonapps.blockchain.ton.contract.WalletVersion
import com.tonapps.blockchain.ton.extensions.toAccountId
import com.tonapps.blockchain.ton.extensions.toRawAddress
import com.tonapps.blockchain.ton.extensions.toWalletAddress
import com.tonapps.wallet.data.account.Wallet
import kotlinx.parcelize.Parcelize
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.api.pub.PublicKeyEd25519
import org.ton.cell.Cell
import org.ton.contract.wallet.WalletTransfer

data class WalletEntity(
    val id: String,
    val publicKey: PublicKeyEd25519,
    val type: Wallet.Type,
    val version: WalletVersion,
    val label: Wallet.Label,
    val ledger: Ledger? = null,
) {

    companion object {
        const val WORKCHAIN = 0
    }

    @Parcelize
    data class Ledger(
        val deviceId: String,
        val accountIndex: Int
    ) : Parcelable

    val contract: BaseWalletContract by lazy {
        val network = if (testnet) TonNetwork.TESTNET.value else TonNetwork.MAINNET.value

        BaseWalletContract.create(publicKey, version.title, network)
    }

    val testnet: Boolean
        get() = type == Wallet.Type.Testnet

    val signer: Boolean
        get() = type == Wallet.Type.Signer || type == Wallet.Type.SignerQR

    val hasPrivateKey: Boolean
        get() = type == Wallet.Type.Default || type == Wallet.Type.Testnet || type == Wallet.Type.Lockup

    val isSigner: Boolean
        get() = type == Wallet.Type.Signer || type == Wallet.Type.SignerQR

    val accountId: String = contract.address.toAccountId()

    val address: String = contract.address.toWalletAddress(testnet)

    val isWatchOnly: Boolean
        get() = type == Wallet.Type.Watch

    val isLedger: Boolean
        get() = type == Wallet.Type.Ledger

    val isExternal: Boolean
        get() = signer || isLedger

    fun isMyAddress(address: String): Boolean {
        return address.toRawAddress().equals(accountId, ignoreCase = true)
    }

    fun isSupportedFeature(feature: WalletFeature): Boolean {
        return contract.isSupportedFeature(feature)
    }

    fun createBody(
        seqno: Int,
        validUntil: Long,
        gifts: List<WalletTransfer>,
        internalMessage: Boolean = false,
    ): Cell {
        return contract.createTransferUnsignedBody(
            validUntil = validUntil,
            seqno = seqno,
            gifts = gifts.toTypedArray(),
            internalMessage = internalMessage,
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