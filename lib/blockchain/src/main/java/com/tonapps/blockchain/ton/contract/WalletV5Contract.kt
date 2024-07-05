package com.tonapps.blockchain.ton.contract

import org.ton.api.pub.PublicKeyEd25519
import org.ton.block.MessageRelaxed
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.contract.wallet.WalletTransfer
import org.ton.crypto.base64
import org.ton.tlb.CellRef
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.storeRef
import org.ton.tlb.storeTlb

class WalletV5Contract(
    workchain: Int = DEFAULT_WORKCHAIN,
    publicKey: PublicKeyEd25519,
    private val networkGlobalId: Int = -239,
    private val subwalletNumber: Int = 0
) : BaseWalletContract(workchain, publicKey) {

    override fun getStateCell(): Cell {
        return CellBuilder.createCell {
            storeUInt(0, 33)
            storeRef(store())
            storeBits(publicKey.key)
            storeBit(false)
        }
    }

    private fun store(): Cell {
        return CellBuilder.createCell {
            storeUInt(networkGlobalId, 32)
            storeUInt(workchain, 8)
            storeUInt(0, 8) // "v5" to 0
            storeUInt(subwalletNumber, 32)
        }
    }

    override fun getCode(): Cell {
        return CODE
    }

    override fun createTransferUnsignedBody(
        validUntil: Long,
        seqno: Int,
        vararg gifts: WalletTransfer
    ): Cell {
        throw NotImplementedError("Not implemented")
    }

    companion object {

        enum class OpCodes(val code: Long) {
            ActionSendMsg(0x0ec3c86d),
            ActionSetCode(0xad4de08e),
            ActionExtendedSetData(0x1ff8ea0b),
            ActionExtendedAddExtension(0x1c40db9f),
            ActionExtendedRemoveExtension(0x5eaef4a4),
            ActionExtendedSetSignatureAuthAllowed(0x20cbb95a),
            AuthExtension(0x6578746e),
            AuthSigned(0x7369676e),
            AuthSignedInternal(0x73696e74)
        }

        @JvmField
        val CODE =
            BagOfCells(base64("te6cckEBAQEAIwAIQgLkzzsvTG1qYeoPK1RH0mZ4WyavNjfbLe7mvNGqgm80Eg3NjhE=")).first()
    }

}