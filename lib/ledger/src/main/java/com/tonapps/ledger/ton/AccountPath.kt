package com.tonapps.ledger.ton

import android.os.Parcelable
import com.tonapps.blockchain.ton.contract.WalletV4R2Contract
import kotlinx.parcelize.Parcelize
import org.ton.api.pub.PublicKeyEd25519
import java.nio.ByteBuffer

@Parcelize
data class AccountPath(
    val index: Int,
    val isTestnet: Boolean = false,
    val workchain: Int = 0
): Parcelable {
    private fun getPath(): List<Int> {
        val network = if (isTestnet) 1 else 0
        val chain = if (workchain == -1) 255 else 0
        return listOf(44, 607, network, chain, index, 0)
    }

    fun toByteArray(): ByteArray {
        val paths = getPath()
        return ByteBuffer.allocate(1 + paths.size * 4).apply {
            put(paths.size.toByte())
            paths.map { (it + 0x80000000).toInt() }.forEach(this::putInt)
        }.array()
    }

    fun contract(publicKey: PublicKeyEd25519): WalletV4R2Contract {
        return WalletV4R2Contract(workchain, publicKey)
    }
}
