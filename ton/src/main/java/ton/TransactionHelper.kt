package ton

import com.tonapps.security.Security
import com.tonapps.security.hex
import core.extensions.toByteArray
import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.block.StateInit
import org.ton.cell.Cell
import org.ton.contract.wallet.WalletTransfer
import org.ton.contract.wallet.WalletTransferBuilder
import java.math.BigInteger

object TransactionHelper {
    fun getWalletQueryId(): BigInteger {
        try {
            val tonkeeperSignature = 0x546de4ef.toByteArray()
            val randomBytes = Security.randomBytes(4)
            val value = tonkeeperSignature + randomBytes
            val hexString = hex(value)
            return BigInteger(hexString, 16)
        } catch (e: Throwable) {
            return BigInteger.ZERO
        }
    }

    fun buildWalletTransfer(
        destination: MsgAddressInt,
        stateInit: StateInit?,
        body: Cell,
        coins: Coins
    ): WalletTransfer {
        val builder = WalletTransferBuilder()
        builder.bounceable = true
        builder.destination = destination
        builder.body = body
        builder.sendMode = SendMode.PAY_GAS_SEPARATELY.value
        builder.coins = coins
        builder.stateInit = stateInit
        return builder.build()
    }

}