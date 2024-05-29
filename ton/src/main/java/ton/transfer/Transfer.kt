package ton.transfer

import android.os.Build
import core.extensions.toByteArray
import org.ton.bigint.BigInt
import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.cell.Cell
import org.ton.cell.buildCell
import org.ton.tlb.CellRef
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.storeRef
import org.ton.tlb.storeTlb
import java.math.BigInteger
import java.security.SecureRandom

object OpCodes {
    const val JETTON_TRANSFER = 0xf8a7ea5
    const val NFT_TRANSFER = 0x5fcc3d14
    const val STONFI_SWAP = 0x25938561
}

object Transfer {

    fun text(text: String?): Cell? {
        if (text.isNullOrEmpty()) {
            return null
        }

        return buildCell {
            storeUInt(0, 32)
            storeBytes(text.toByteArray())
        }
    }

    fun body(body: Any?): Cell? {
        if (body == null) {
            return null
        }
        return when (body) {
            is String -> text(body)
            is Cell -> body
            else -> null
        }
    }

    fun jetton(
        coins: Coins,
        toAddress: MsgAddressInt,
        responseAddress: MsgAddressInt,
        queryId: BigInt = BigInt.ZERO,
        forwardAmount: Coins = Coins.ofNano(1L),
        forwardPayload: Any? = null,
    ): Cell {
        val payload = body(forwardPayload)

        return buildCell {
            storeUInt(OpCodes.JETTON_TRANSFER, 32)
            storeUInt(queryId, 64)
            storeTlb(Coins, coins)
            storeTlb(MsgAddressInt, toAddress)
            storeTlb(MsgAddressInt, responseAddress)
            storeBit(false)
            storeTlb(Coins, forwardAmount)
            if (payload == null) {
                storeBit(false)
            } else {
                storeBit(true)
                storeRef(AnyTlbConstructor, CellRef(payload))
            }
        }
    }

    fun nft(
        newOwnerAddress: MsgAddressInt,
        excessesAddress: MsgAddressInt,
        queryId: BigInt = BigInt.ZERO,
        forwardAmount: Coins = Coins.ofNano(1L),
        body: Any? = null,
    ): Cell {
        val payload = body(body)

        return buildCell {
            storeUInt(OpCodes.NFT_TRANSFER, 32)
            storeUInt(queryId, 64)
            storeTlb(MsgAddressInt, newOwnerAddress)
            storeTlb(MsgAddressInt, excessesAddress)
            storeBit(false)
            storeTlb(Coins, forwardAmount)
            storeBit(payload != null)
            payload?.let {
                storeRef(AnyTlbConstructor, CellRef(it))
            }
        }
    }

    fun swap(
        assetToSwap: MsgAddressInt,
        minAskAmount: Coins,
        userWalletAddress: MsgAddressInt,
        referralAddress: MsgAddressInt? = null
    ): Cell {
        return buildCell {
            storeUInt(OpCodes.STONFI_SWAP, 32)
            storeTlb(MsgAddressInt, assetToSwap)
            storeTlb(Coins, minAskAmount)
            storeTlb(MsgAddressInt, userWalletAddress)

            if (referralAddress != null) {
                storeBit(true)
                storeTlb(MsgAddressInt, referralAddress)
            } else {
                storeBit(false)
            }
        }
    }

}

object QueryId {
    private const val TONKEEPER_SIGNATURE = 0x546de4ef

    fun newQueryId(): BigInteger {
        try {
            val tonkeeperSignature = TONKEEPER_SIGNATURE.toByteArray()
            val randomBytes = Security.randomBytes(4)
            val value = tonkeeperSignature + randomBytes
            val hexString = HexUtils.hex(value)
            return BigInteger(hexString, 16)
        } catch (e: Throwable) {
            return System.currentTimeMillis().toBigInteger()
        }
    }
}

internal object HexUtils {
    private val DIGITS = "0123456789abcdef".toCharArray()

    fun hex(bytes: ByteArray): String = buildString(bytes.size * 2) {
        bytes.forEach { byte ->
            val b = byte.toInt() and 0xFF
            append(DIGITS[b shr 4])
            append(DIGITS[b and 0x0F])
        }
    }
}

internal object Security {
    fun randomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        secureRandom().nextBytes(bytes)
        return bytes
    }

    private fun secureRandom(): SecureRandom =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SecureRandom.getInstanceStrong()
        } else {
            SecureRandom()
        }
}