package com.tonapps.blockchain.ton.contract

import com.tonapps.blockchain.ton.tlb.CellStringTlbConstructor
import kotlinx.io.bytestring.ByteString
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.api.pub.PublicKeyEd25519
import org.ton.bitstring.BitString
import org.ton.block.AddrNone
import org.ton.block.AddrStd
import org.ton.block.Coins
import org.ton.block.CommonMsgInfoRelaxed
import org.ton.block.Either
import org.ton.block.ExtInMsgInfo
import org.ton.block.Maybe
import org.ton.block.Message
import org.ton.block.MessageRelaxed
import org.ton.block.MsgAddressInt
import org.ton.block.StateInit
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.buildCell
import org.ton.contract.SmartContract
import org.ton.contract.wallet.WalletTransfer
import org.ton.tlb.CellRef
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.storeTlb
import java.math.BigInteger

abstract class BaseWalletContract(
    val workchain: Int = DEFAULT_WORKCHAIN,
    val publicKey: PublicKeyEd25519
) {

    companion object {
        const val DEFAULT_WORKCHAIN = 0
        const val DEFAULT_WALLET_ID: Int = 698983191

        fun create(publicKey: PublicKeyEd25519, v: String, networkGlobalId: Int): BaseWalletContract {
            return when(v.lowercase()) {
                "v3r1" -> WalletV3R1Contract(publicKey = publicKey)
                "v3r2" -> WalletV3R2Contract(publicKey = publicKey)
                "v4r1" -> WalletV4R1Contract(publicKey = publicKey)
                "v4r2" -> WalletV4R2Contract(publicKey = publicKey)
                "v5beta" -> WalletV5BetaContract(publicKey = publicKey, networkGlobalId = networkGlobalId)
                "v5r1" -> WalletV5R1Contract(publicKey = publicKey, networkGlobalId = networkGlobalId)
                else -> throw IllegalArgumentException("Unsupported contract version: $v")
            }
        }

        fun create(publicKey: PublicKeyEd25519, v: String, testnet: Boolean): BaseWalletContract {
            return create(publicKey, v, if (testnet) -3 else -239)
        }

        fun createIntMsg(gift: WalletTransfer): MessageRelaxed<Cell> {
            val info = CommonMsgInfoRelaxed.IntMsgInfoRelaxed(
                ihrDisabled = true,
                bounce = gift.bounceable,
                bounced = false,
                src = AddrNone,
                dest = gift.destination,
                value = gift.coins,
                ihrFee = Coins(),
                fwdFee = Coins(),
                createdLt = 0u,
                createdAt = 0u
            )
            val init = Maybe.of(gift.stateInit?.let {
                Either.of<StateInit, CellRef<StateInit>>(it, null)
            })
            val body = if (gift.body == null) {
                Either.of<Cell, CellRef<Cell>>(Cell.empty(), null)
            } else {
                Either.of<Cell, CellRef<Cell>>(null, CellRef(gift.body!!))
            }

            return MessageRelaxed(
                info = info,
                init = init,
                body = body,
            )
        }
    }

    val walletId = DEFAULT_WALLET_ID + workchain

    val stateInit: StateInit by lazy {
        val cell = getStateCell()
        val code = getCode()
        StateInit(code, cell)
    }

    val address: AddrStd by lazy {
        SmartContract.address(workchain, stateInit)
    }

    abstract val features: WalletFeature

    fun isSupportedFeature(feature: WalletFeature): Boolean {
        return features.contains(feature)
    }

    abstract fun getStateCell(): Cell

    abstract fun getCode(): Cell

    abstract fun getWalletVersion(): WalletVersion

    abstract fun createTransferUnsignedBody(
        validUntil: Long,
        seqno: Int,
        internalMessage: Boolean = false,
        queryId: BigInteger? = null,
        vararg gifts: WalletTransfer
    ): Cell

    private fun signBody(
        privateKey: PrivateKeyEd25519,
        unsignedBody: Cell,
    ): Cell {
        val signature = BitString(privateKey.sign(unsignedBody.hash()))
        return signedBody(signature, unsignedBody)
    }

    open fun signedBody(
        signature: BitString,
        unsignedBody: Cell,
    ) = CellBuilder.createCell {
        storeBits(signature)
        storeBits(unsignedBody.bits)
        storeRefs(unsignedBody.refs)
    }

    fun createTransferMessageCell(
        address: MsgAddressInt,
        privateKey: PrivateKeyEd25519,
        seqno: Int,
        unsignedBody: Cell
    ): Cell {
        val message = createTransferMessage(address, privateKey, seqno, unsignedBody)

        val cell = buildCell {
            storeTlb(Message.tlbCodec(AnyTlbConstructor), message)
        }
        return cell
    }

    fun createTransferMessage(
        address: MsgAddressInt,
        privateKey: PrivateKeyEd25519,
        seqno: Int,
        unsignedBody: Cell
    ): Message<Cell> {
        val info = ExtInMsgInfo(
            src = AddrNone,
            dest = address,
            importFee = Coins()
        )

        val init = if (seqno == 0) {
            stateInit
        } else null

        val maybeStateInit = Maybe.of(init?.let { Either.of<StateInit, CellRef<StateInit>>(null, CellRef(it)) })

        val transferBody = signBody(privateKey, unsignedBody)

        val body = Either.of<Cell, CellRef<Cell>>(null, CellRef(transferBody))
        return Message(
            info = info,
            init = maybeStateInit,
            body = body
        )
    }

    fun createTransferMessage(
        address: MsgAddressInt,
        seqno: Int,
        transferBody: Cell
    ): Message<Cell> {
        val info = ExtInMsgInfo(
            src = AddrNone,
            dest = address,
            importFee = Coins()
        )

        val init = if (seqno == 0) {
            stateInit
        } else null

        val maybeStateInit = Maybe.of(init?.let { Either.of<StateInit, CellRef<StateInit>>(null, CellRef(it)) })

        val body = Either.of<Cell, CellRef<Cell>>(null, CellRef(transferBody))
        return Message(
            info = info,
            init = maybeStateInit,
            body = body
        )
    }

    fun createTransferMessageCell(
        address: MsgAddressInt,
        seqno: Int,
        transferBody: Cell
    ): Cell {
        val message = createTransferMessage(address, seqno, transferBody)

        val cell = buildCell {
            storeTlb(Message.tlbCodec(AnyTlbConstructor), message)
        }
        return cell
    }

    fun createBatteryBody(address: MsgAddressInt? = null, appliedPromo: String? = null): Cell {
        val cell = buildCell {
            storeUInt(0xb7b2515f, 32)
            storeBit(address != null)
            if (address != null) {
                storeTlb(MsgAddressInt, address)
            }
            storeBit(appliedPromo.isNullOrEmpty())
            if (!appliedPromo.isNullOrEmpty()) {
                storeTlb(CellStringTlbConstructor, ByteString(*appliedPromo.encodeToByteArray()))
            }
        }

        return cell
    }
}
