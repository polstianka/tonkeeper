package com.tonapps.blockchain.ton.contract

import com.tonapps.blockchain.ton.TonNetwork
import com.tonapps.blockchain.ton.contract.WalletV5R1Contract.W5Context.Client
import com.tonapps.blockchain.ton.contract.WalletV5R1Contract.W5Context.Custom
import com.tonapps.blockchain.ton.extensions.storeBuilder
import com.tonapps.blockchain.ton.extensions.storeSeqAndValidUntil
import org.ton.api.pub.PublicKeyEd25519
import org.ton.bigint.BigInt
import org.ton.bigint.toBigInt
import org.ton.bitstring.BitString
import org.ton.block.MessageRelaxed
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.buildCell
import org.ton.contract.wallet.WalletTransfer
import org.ton.crypto.hex
import org.ton.tlb.CellRef
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.storeRef

class WalletV5R1Contract(
    publicKey: PublicKeyEd25519,
    private val context: W5Context,
) : BaseWalletContract(context.getWorkchain(), publicKey) {

    sealed class W5Context(
        open val networkGlobalId: Int,
    ) {

        val walletId: BigInt by lazy {
            cell().beginParse().loadInt(32)
        }

        val serialized: BigInt by lazy {
            networkGlobalId.toBigInt().xor(walletId)
        }

        open fun cell(): Cell {
            return Cell()
        }

        data class Client(
            val workchain: Int = DEFAULT_WORKCHAIN,
            val subwalletNumber: Int = 0,
            override val networkGlobalId: Int = -239
        ): W5Context(networkGlobalId) {


            override fun cell() = CellBuilder.createCell {
                storeUInt(1, 1)
                storeUInt(workchain, 8)
                storeUInt(0, 8)
                storeUInt(subwalletNumber, 15)
            }
        }

        data class Custom(
            val id: Int,
            override val networkGlobalId: Int = -239
        ): W5Context(networkGlobalId) {

            override fun cell() = CellBuilder.createCell {
                storeInt(0, 1)
                storeInt(id, 31)
            }
        }

    }

    constructor(publicKey: PublicKeyEd25519, network: TonNetwork) : this(
        publicKey = publicKey,
        networkGlobalId = network.value
    )

    constructor(publicKey: PublicKeyEd25519, networkGlobalId: Int) : this(
        publicKey = publicKey,
        context = Client(networkGlobalId = networkGlobalId)
    )

    override fun getStateCell(): Cell {
        return CellBuilder.createCell {
            storeUInt(1, 1)
            storeUInt(0, 32)
            storeInt(context.serialized, 32)
            storeBits(publicKey.key)
            storeBit(false)
        }
    }

    override fun getWalletVersion() = WalletVersion.V5R1

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
            storeUInt(context.serialized, 32)
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
            storeUInt(1, 1)
            storeRef(list)
            storeUInt(0, 1)
        }
    }

    companion object {

        private fun W5Context.getWorkchain(): Int {
            return when (this) {
                is Client -> workchain
                is Custom -> DEFAULT_WORKCHAIN
            }
        }

        @JvmField
        val CODE =
            BagOfCells(hex("b5ee9c7241021401000281000114ff00f4a413f4bcf2c80b01020120020d020148030402dcd020d749c120915b8f6320d70b1f2082106578746ebd21821073696e74bdb0925f03e082106578746eba8eb48020d72101d074d721fa4030fa44f828fa443058bd915be0ed44d0810141d721f4058307f40e6fa1319130e18040d721707fdb3ce03120d749810280b99130e070e2100f020120050c020120060902016e07080019adce76a2684020eb90eb85ffc00019af1df6a2684010eb90eb858fc00201480a0b0017b325fb51341c75c875c2c7e00011b262fb513435c280200019be5f0f6a2684080a0eb90fa02c0102f20e011e20d70b1f82107369676ebaf2e08a7f0f01e68ef0eda2edfb218308d722028308d723208020d721d31fd31fd31fed44d0d200d31f20d31fd3ffd70a000af90140ccf9109a28945f0adb31e1f2c087df02b35007b0f2d0845125baf2e0855036baf2e086f823bbf2d0882292f800de01a47fc8ca00cb1f01cf16c9ed542092f80fde70db3cd81003f6eda2edfb02f404216e926c218e4c0221d73930709421c700b38e2d01d72820761e436c20d749c008f2e09320d74ac002f2e09320d71d06c712c2005230b0f2d089d74cd7393001a4e86c128407bbf2e093d74ac000f2e093ed55e2d20001c000915be0ebd72c08142091709601d72c081c12e25210b1e30f20d74a111213009601fa4001fa44f828fa443058baf2e091ed44d0810141d718f405049d7fc8ca0040048307f453f2e08b8e14038307f45bf2e08c22d70a00216e01b3b0f2d090e2c85003cf1612f400c9ed54007230d72c08248e2d21f2e092d200ed44d0d2005113baf2d08f54503091319c01810140d721d70a00f2e08ee2c8ca0058cf16c9ed5493f2c08de20010935bdb31e1d74cd0b4d6c35e")).first()
    }

}