package com.tonapps.tonkeeper.fragment.swap.domain

import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.cell.Cell
import org.ton.cell.buildCell
import org.ton.tlb.storeTlb

/**
 * jettonToWalletAddress -> assetToSwap
 * minAskAmount -> minAskAmount
 * userWalletAddress -> userWalletAddress
 *
 * */
class CreateSwapCellCase {

    fun execute(
        assetToSwap: String,
        minAskAmount: Coins,
        userWalletAddress: MsgAddressInt
    ): Cell {
        return buildCell {
            // opcode
            storeUInt(0x25938561, 32)

            // assetToSwap
            storeTlb(
                MsgAddressInt.tlbCodec(),
                MsgAddressInt.parse(assetToSwap)
            )
            //minAskAmount
            storeTlb(Coins, minAskAmount)
            // userWalletAddress
            storeTlb(
                MsgAddressInt.tlbCodec(),
                userWalletAddress
            )
            // no referralAddress
            storeUInt(0, 1)
        }
    }
}