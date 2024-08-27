package com.tonapps.blockchain.ton.contract

import org.ton.api.pub.PublicKeyEd25519
import org.ton.bitstring.BitString
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.contract.wallet.WalletTransfer
import org.ton.crypto.base64
import java.math.BigInteger

class LockupContractV1(
    publicKey: PublicKeyEd25519,
    private val configPubKey: BitString,
    private val allowedDestinations: Boolean
): BaseWalletContract(publicKey = publicKey) {

    override val features: WalletFeature = WalletFeature.NONE

    override fun getStateCell(): Cell {
        return CellBuilder.createCell {
            storeUInt(0, 32)
            storeUInt(walletId, 32)
            storeBits(publicKey.key)
            storeBits(configPubKey)
        }
    }

    override fun getCode(): Cell {
        return CODE
    }

    override fun getWalletVersion() = WalletVersion.UNKNOWN

    override fun createTransferUnsignedBody(
        validUntil: Long,
        seqno: Int,
        internalMessage: Boolean,
        queryId: BigInteger?,
        vararg gifts: WalletTransfer
    ): Cell {
        TODO("Not yet implemented")
    }


    companion object {
        @JvmField
        val CODE =
            BagOfCells(base64("te6ccsECHgEAAmEAAAAADQASABcAHAAhACYApwCvALwAxgDSAOsA8AD1ARIBNgFbAWABZQFqAYMBiAGWAaQBqQG0AcIBzwJLART/APSkE/S88sgLAQIBIAIcAgFIAxECAs0EDAIBIAULAgEgBgoD9wB0NMDAXGwkl8D4PpAMCHHAJJfA+AB0x8hwQKSXwTg8ANRtPABghCC6vnEUrC9sJJfDOCAKIIQgur5xBu6GvL0gCErghA7msoAvvL0B4MI1xiAICH5AVQQNvkQEvL00x+AKYIQNzqp9BO6EvL00wDTHzAB4w8QSBA3XjKAHCAkADBA5SArwBQAWEDdBCvAFCBBXUFYAEBAkQwDwBO1UABMIddJ9KhvpWwxgAC1e1E0NMf0x/T/9P/9AT6APQE+gD0BNGAIBIA0QAgEgDg8ANQIyMofF8ofFcv/E8v/9AAB+gL0AAH6AvQAyYABDFEioFMTgCD0Dm+hlvoA0ROgApEw4shQA/oCQBOAIPRDAYABFSOHiKAIPSWb6UgkzAju5Ex4iCYNfoA0ROhQBOSbCHis+YwgCASASGwIBIBMYAgEgFBUALbUYfgBtiIaKgmCeAMYgfgDGPwTt4gswAgFYFhcAF63OdqJoaZ+Y64X/wAAXrHj2omhpj5jrhY/AAgFIGRoAEbMl+1E0NcLH4AAXsdG+COCAQjD7UPYgABW96feAGIJC+EeADAHy8oMI1xgg0x/TH9MfgCQD+CO7E/Ly8AOAIlGpuhry9IAjUbe6G/L0gB8L+QFUEMX5EBry9PgAUFf4I/AGUJj4I/AGIHEokyDXSo6L0wcx1FEb2zwSsAHoMJIpoN9y+wIGkyDXSpbTB9QC+wDo0QOkR2gUFUMw8ATtVB0AKAHQ0wMBeLCSW3/g+kAx+kAwAfAB2Ae6sw==")).first()
    }
}