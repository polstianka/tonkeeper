package com.tonapps.tonkeeper.core.entities

import android.util.Log
import com.tonapps.blockchain.ton.TonSendMode
import com.tonapps.blockchain.ton.TonTransferHelper
import com.tonapps.blockchain.ton.contract.BaseWalletContract
import com.tonapps.blockchain.ton.extensions.toAccountId
import com.tonapps.blockchain.ton.tlb.MessageData
import com.tonapps.extensions.toByteArray
import com.tonapps.icu.Coins
import com.tonapps.ledger.ton.TonPayloadFormat
import com.tonapps.ledger.ton.Transaction
import com.tonapps.ledger.ton.TransactionBuilder
import com.tonapps.security.Security
import com.tonapps.security.hex
import com.tonapps.wallet.api.entity.BalanceEntity
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.events.CommentEncryption
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.api.pub.PublicKeyEd25519
import org.ton.bitstring.BitString
import org.ton.block.AddrStd
import org.ton.block.StateInit
import org.ton.cell.Cell
import org.ton.contract.wallet.WalletTransfer
import org.ton.contract.wallet.WalletTransferBuilder
import java.math.BigInteger

data class TransferEntity(
    val wallet: WalletEntity,
    val token: BalanceEntity,
    val destination: AddrStd,
    val destinationPK: PublicKeyEd25519,
    val amount: Coins,
    val max: Boolean,
    val seqno: Int,
    val validUntil: Long,
    val bounceable: Boolean,
    val comment: String?,
    val nftAddress: String? = null,
    val commentEncrypted: Boolean,
    val queryId: BigInteger = newWalletQueryId(),
) {

    val contract: BaseWalletContract
        get() = wallet.contract

    val isTon: Boolean
        get() = token.isTon

    val isNft: Boolean
        get() = nftAddress != null

    val stateInit: StateInit?
        get() = if (seqno == 0) contract.stateInit else null

    val testnet: Boolean
        get() = wallet.testnet

    val sendMode: Int
        get() {
            return if (max && isTon) (TonSendMode.CARRY_ALL_REMAINING_BALANCE.value + TonSendMode.IGNORE_ERRORS.value) else (TonSendMode.PAY_GAS_SEPARATELY.value + TonSendMode.IGNORE_ERRORS.value)
        }

    private val coins: org.ton.block.Coins
        get() {
            return org.ton.block.Coins.ofNano(amount.toLong())
        }

    private fun getCommentForwardPayload(
        privateKey: PrivateKeyEd25519? = null
    ): Cell? {
        if (comment.isNullOrBlank()) {
            return null
        } else if (!commentEncrypted) {
            return MessageData.text(comment).body
        } else {
            privateKey
                ?: throw IllegalArgumentException("Private key required for encrypted comment")

            val publicKey = privateKey.publicKey()
            return CommentEncryption.encryptComment(
                comment = comment,
                myPublicKey = publicKey,
                theirPublicKey = destinationPK,
                myPrivateKey = privateKey,
                senderAddress = contract.address.toAccountId()
            )
        }
    }

    private fun getWalletTransfer(
        privateKey: PrivateKeyEd25519?,
        excessesAddress: AddrStd
    ): WalletTransfer {
        val builder = WalletTransferBuilder()
        builder.bounceable = bounceable
        builder.body = body(privateKey, excessesAddress)
        builder.sendMode = sendMode
        if (isNft) {
            builder.coins = coins
            builder.destination = AddrStd.parse(nftAddress!!)
        } else if (!isTon) {
            builder.coins = TRANSFER_PRICE
            builder.destination = AddrStd.parse(token.walletAddress)
        } else {
            builder.coins = coins
            builder.destination = destination
        }
        builder.stateInit = stateInit
        return builder.build()
    }

    private fun getGifts(
        privateKey: PrivateKeyEd25519?,
        excessesAddress: AddrStd
    ): Array<WalletTransfer> {
        return arrayOf(getWalletTransfer(privateKey, excessesAddress))
    }

    fun getUnsignedBody(
        privateKey: PrivateKeyEd25519? = null,
        internalMessage: Boolean = false,
        excessesAddress: AddrStd? = null
    ): Cell {
        return contract.createTransferUnsignedBody(
            validUntil = validUntil,
            seqno = seqno,
            gifts = getGifts(privateKey, excessesAddress = excessesAddress ?: contract.address),
            internalMessage = internalMessage ?: false,
        )
    }

    fun getLedgerTransaction(): Transaction {
        val builder = TransactionBuilder()
        if (isNft) {
            builder.setCoins(coins)
            builder.setDestination(AddrStd.parse(nftAddress!!))
            builder.setPayload(
                TonPayloadFormat.NftTransfer(
                    queryId = queryId,
                    newOwnerAddress = destination,
                    excessesAddress = contract.address,
                    forwardPayload = getCommentForwardPayload(),
                    forwardAmount = org.ton.block.Coins.ofNano(1L),
                    customPayload = null
                )
            )
        } else if (!isTon) {
            builder.setCoins(TRANSFER_PRICE)
            builder.setDestination(AddrStd.parse(token.walletAddress))
            builder.setPayload(
                TonPayloadFormat.JettonTransfer(
                    queryId = queryId,
                    coins = coins,
                    receiverAddress = destination,
                    excessesAddress = contract.address,
                    forwardPayload = getCommentForwardPayload(),
                    forwardAmount = org.ton.block.Coins.ofNano(1L),
                    customPayload = null
                )
            )
        } else {
            builder.setCoins(coins)
            builder.setDestination(destination)
            if (comment != null) {
                builder.setPayload(TonPayloadFormat.Comment(comment))
            }
        }
        builder.setSendMode(sendMode)
        builder.setSeqno(seqno)
        builder.setTimeout(validUntil.toInt())
        builder.setBounceable(bounceable)
        builder.setStateInit(stateInit)
        return builder.build()
    }

    fun signedHash(privateKey: PrivateKeyEd25519): BitString {
        return BitString(privateKey.sign(getUnsignedBody(privateKey).hash()))
    }

    private fun messageBodyWithSign(
        signature: BitString
    ): Cell {
        val unsignedBody = getUnsignedBody()
        return contract.signedBody(signature, unsignedBody)
    }

    fun transferMessage(
        signature: BitString
    ): Cell {
        val signedBody = messageBodyWithSign(signature)
        return contract.createTransferMessageCell(contract.address, seqno, signedBody)
    }

    fun transferMessage(signedBody: Cell): Cell {
        return contract.createTransferMessageCell(contract.address, seqno, signedBody)
    }

    private fun body(privateKey: PrivateKeyEd25519?, excessesAddress: AddrStd): Cell? {
        if (isNft) {
            return nftBody(privateKey, excessesAddress)
        } else if (!isTon) {
            return jettonBody(privateKey, excessesAddress)
        }
        return getCommentForwardPayload(privateKey)
    }

    private fun jettonBody(privateKey: PrivateKeyEd25519?, excessesAddress: AddrStd): Cell {
        return TonTransferHelper.jetton(
            coins = coins,
            toAddress = destination,
            responseAddress = excessesAddress,
            queryId = queryId,
            body = getCommentForwardPayload(privateKey),
        )
    }

    private fun nftBody(privateKey: PrivateKeyEd25519?, excessesAddress: AddrStd): Cell {
        return TonTransferHelper.nft(
            newOwnerAddress = destination,
            excessesAddress = excessesAddress,
            queryId = queryId,
            body = getCommentForwardPayload(privateKey),
        )
    }

    fun toSignedMessage(
        privateKeyEd25519: PrivateKeyEd25519,
        isBattery: Boolean,
        excessesAddress: AddrStd? = null
    ): Cell {
        return contract.createTransferMessageCell(
            address = contract.address,
            privateKey = privateKeyEd25519,
            seqno = seqno,
            unsignedBody = getUnsignedBody(
                privateKeyEd25519,
                internalMessage = isBattery,
                excessesAddress = excessesAddress,
            ),
        )
    }

    class Builder(private val wallet: WalletEntity) {
        private var token: BalanceEntity? = null
        private var destination: AddrStd? = null
        private var destinationPK: PublicKeyEd25519? = null
        private var amount: Coins? = null
        private var max: Boolean = false
        private var seqno: Int? = null
        private var validUntil: Long? = null
        private var bounceable: Boolean = false
        private var comment: String? = null
        private var commentEncrypted: Boolean = false
        private var nftAddress: String? = null
        private var queryId: BigInteger? = null

        fun setQueryId(queryId: BigInteger) = apply { this.queryId = queryId }

        fun setNftAddress(nftAddress: String) = apply { this.nftAddress = nftAddress }

        fun setToken(token: BalanceEntity) = apply { this.token = token }

        fun setDestination(destination: AddrStd, destinationPK: PublicKeyEd25519) = apply {
            this.destination = destination
            this.destinationPK = destinationPK
        }

        fun setAmount(amount: Coins) = apply { this.amount = amount }

        fun setMax(max: Boolean = true) = apply { this.max = max }

        fun setSeqno(seqno: Int) = apply { this.seqno = seqno }

        fun setValidUntil(validUntil: Long) = apply { this.validUntil = validUntil }

        fun setBounceable(bounceable: Boolean = false) = apply { this.bounceable = bounceable }

        fun setComment(comment: String?, commentEncrypted: Boolean) = apply {
            this.comment = comment
            this.commentEncrypted = commentEncrypted
        }

        fun build(): TransferEntity {
            val token = token ?: throw IllegalArgumentException("Token is not set")
            val destination =
                destination ?: throw IllegalArgumentException("Destination is not set")
            val destinationPK =
                destinationPK ?: throw IllegalArgumentException("DestinationPK is not set")
            val amount = amount ?: throw IllegalArgumentException("Amount is not set")
            val seqno = seqno ?: throw IllegalArgumentException("Seqno is not set")
            val validUntil = validUntil ?: throw IllegalArgumentException("ValidUntil is not set")
            return TransferEntity(
                wallet = wallet,
                token = token,
                destination = destination,
                destinationPK = destinationPK,
                amount = amount,
                max = max,
                seqno = seqno,
                validUntil = validUntil,
                bounceable = bounceable,
                comment = comment,
                nftAddress = nftAddress,
                commentEncrypted = commentEncrypted,
                queryId = queryId ?: newWalletQueryId()
            )
        }
    }

    companion object {

        private val TRANSFER_PRICE = org.ton.block.Coins.ofNano(Coins.of(0.064, 9).toLong())

        fun newWalletQueryId(): BigInteger {
            return try {
                val tonkeeperSignature = 0x546de4ef.toByteArray()
                val randomBytes = Security.randomBytes(4)
                val value = tonkeeperSignature + randomBytes
                val hexString = hex(value)
                BigInteger(hexString, 16)
            } catch (e: Throwable) {
                BigInteger.ZERO
            }
        }
    }
}