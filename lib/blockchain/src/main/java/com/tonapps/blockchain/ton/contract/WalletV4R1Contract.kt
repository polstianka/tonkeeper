package com.tonapps.blockchain.ton.contract

import com.tonapps.blockchain.ton.extensions.storeSeqAndValidUntil
import org.ton.api.pub.PublicKeyEd25519
import org.ton.bigint.BigInt
import org.ton.block.MessageRelaxed
import org.ton.block.StateInit
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.contract.wallet.WalletTransfer
import org.ton.crypto.base64
import org.ton.tlb.CellRef
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.storeRef

open class WalletV4R1Contract(
    workchain: Int = DEFAULT_WORKCHAIN,
    publicKey: PublicKeyEd25519,
) : BaseWalletContract(workchain, publicKey) {

    override val features: WalletFeature = WalletFeature.NONE

    override fun getWalletVersion() = WalletVersion.V4R1

    override fun getStateCell(): Cell {
        return CellBuilder.createCell {
            storeUInt(0, 32)
            storeUInt(walletId, 32)
            storeBits(publicKey.key)
            storeBit(false)
        }
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
        if (gifts.size > 4) {
            throw IllegalArgumentException("Maximum number of messages in a single transfer is 4")
        }
        return CellBuilder.createCell {
            storeUInt(walletId, 32)
            storeSeqAndValidUntil(seqno, validUntil)
            storeUInt(0, 8)
            for (gift in gifts) {
                var sendMode = 3
                if (gift.sendMode > -1) {
                    sendMode = gift.sendMode
                }
                storeUInt(sendMode, 8)

                val intMsg = CellRef(createIntMsg(gift))
                storeRef(MessageRelaxed.tlbCodec(AnyTlbConstructor), intMsg)
            }
        }
    }

    companion object {

        @JvmField
        val CODE =
            BagOfCells(base64("te6cckECFQEAAvUAART/APSkE/S88sgLAQIBIAIQAgFIAwcD7tAB0NMDAXGwkVvgIddJwSCRW+AB0x8hghBwbHVnvSKCEGJsbmO9sCKCEGRzdHK9sJJfA+AC+kAwIPpEAcjKB8v/ydDtRNCBAUDXIfQEMFyBAQj0Cm+hMbOSXwXgBNM/yCWCEHBsdWe6kTHjDSSCEGJsbmO64wAEBAUGAFAB+gD0BDCCEHBsdWeDHrFwgBhQBcsFJ88WUAP6AvQAEstpyx9SEMs/AFL4J28ighBibG5jgx6xcIAYUAXLBSfPFiT6AhTLahPLH1Iwyz8B+gL0AACSghBkc3Ryuo41BIEBCPRZMO1E0IEBQNcgyAHPFvQAye1UghBkc3Rygx6xcIAYUATLBVjPFiL6AhLLassfyz+UEDRfBOLJgED7AAIBIAgPAgEgCQ4CAVgKCwA9sp37UTQgQFA1yH0BDACyMoHy//J0AGBAQj0Cm+hMYAIBIAwNABmtznaiaEAga5Drhf/AABmvHfaiaEAQa5DrhY/AABG4yX7UTQ1wsfgAWb0kK29qJoQICga5D6AhhHDUCAhHpJN9KZEM5pA+n/mDeBKAG3gQFImHFZ8xhAT48oMI1xgg0x/TH9MfAvgju/Jj7UTQ0x/TH9P/9ATRUUO68qFRUbryogX5AVQQZPkQ8qP4ACSkyMsfUkDLH1Iwy/9SEPQAye1U+A8B0wchwACfbFGTINdKltMH1AL7AOgw4CHAAeMAIcAC4wABwAORMOMNA6TIyx8Syx/L/xESExQAbtIH+gDU1CL5AAXIygcVy//J0Hd0gBjIywXLAiLPFlAF+gIUy2sSzMzJcfsAyEAUgQEI9FHypwIAbIEBCNcYyFQgJYEBCPRR8qeCEG5vdGVwdIAYyMsFywJQBM8WghAF9eEA+gITy2oSyx/JcfsAAgBygQEI1xgwUgKBAQj0WfKn+CWCEGRzdHJwdIAYyMsFywJQBc8WghAF9eEA+gIUy2oTyx8Syz/Jc/sAAAr0AMntVHbNOpo=")).first()
    }
}