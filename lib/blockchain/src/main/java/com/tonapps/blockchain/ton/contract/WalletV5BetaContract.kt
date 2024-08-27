package com.tonapps.blockchain.ton.contract

import com.tonapps.blockchain.ton.extensions.storeBuilder
import com.tonapps.blockchain.ton.extensions.storeSeqAndValidUntil
import org.ton.api.pub.PublicKeyEd25519
import org.ton.bigint.BigInt
import org.ton.bitstring.BitString
import org.ton.block.MessageRelaxed
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.buildCell
import org.ton.contract.wallet.WalletTransfer
import org.ton.crypto.base64
import org.ton.tlb.CellRef
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.storeRef

class WalletV5BetaContract(
    workchain: Int = DEFAULT_WORKCHAIN,
    publicKey: PublicKeyEd25519,
    private val networkGlobalId: Int = -239,
    private val subwalletNumber: Int = 0
) : BaseWalletContract(workchain, publicKey) {

    override val features: WalletFeature = WalletFeature.GASLESS and WalletFeature.SIGNED_INTERNALS

    override fun getWalletVersion() = WalletVersion.V5BETA

    override fun getStateCell(): Cell {
        return CellBuilder.createCell {
            storeUInt(0, 33)
            storeSlice(storeWalletId())
            storeBits(publicKey.key)
            storeBit(false)
        }
    }

    private fun storeWalletId(): CellSlice {
        return CellBuilder.createCell {
            storeInt(networkGlobalId, 32)
            storeInt(workchain, 8)
            storeUInt(0, 8) // "v5" to 0
            storeUInt(subwalletNumber, 32)
        }.beginParse()
    }

    override fun signedBody(
        signature: BitString,
        unsignedBody: Cell
    ) = CellBuilder.createCell {
        storeBits(unsignedBody.bits)
        storeBits(signature)
        storeRefs(unsignedBody.refs)
    }

    override fun getCode(): Cell {
        return CODE
    }

    override fun createTransferUnsignedBody(
        validUntil: Long,
        seqno: Int,
        internalMessage: Boolean,
        queryId: BigInt?,
        vararg gifts: WalletTransfer
    ): Cell {
        if (gifts.size > 255) {
            throw IllegalArgumentException("Maximum number of messages in a single transfer is 255")
        }

        val actions = packV5Actions(*gifts)

        val opCode = if (internalMessage) 0x73696e74 else 0x7369676e

        return buildCell {
            storeUInt(opCode, 32)
            storeInt(networkGlobalId, 32)
            storeInt(workchain, 8)
            storeUInt(0, 8)
            storeUInt(subwalletNumber, 32)
            storeSeqAndValidUntil(seqno, validUntil)
            storeBuilder(actions)
        }
    }

    private fun packV5Actions(vararg gifts: WalletTransfer): CellBuilder {
        var list = Cell.empty()

        for (gift in gifts) {
            val intMsg = CellRef(createIntMsg(gift))

            val msg = CellBuilder.beginCell().apply {
                storeUInt(0x0ec3c86d, 32)
                storeUInt(gift.sendMode, 8)
                storeRef(MessageRelaxed.tlbCodec(AnyTlbConstructor), intMsg)
            }

            list = buildCell {
                storeRef(list)
                storeBuilder(msg)
            }
        }

        return CellBuilder.beginCell().apply {
            storeUInt(0, 1)
            storeRef(list)
        }
    }

    companion object {

        @JvmField
        val CODE =
            BagOfCells(base64("te6cckEBAQEAIwAIQgLkzzsvTG1qYeoPK1RH0mZ4WyavNjfbLe7mvNGqgm80Eg3NjhE=")).first()
    }

}