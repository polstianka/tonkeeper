package com.tonapps.tonkeeper.core.signer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.tonapps.blockchain.ton.extensions.hex
import org.ton.api.pub.PublicKeyEd25519
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.crypto.base64
import org.ton.crypto.hex
import ton.extensions.base64

object SignerApp {

    private const val STORE_LINK = "https://play.google.com/store/apps/details?id=com.tonapps.singer"

    fun openAppOrInstall(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tonsign://"))
            open(context, intent)
        } catch (e: Throwable) {
            openStore(context)
        }
    }

    private fun openStore(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(STORE_LINK))
        open(context, intent)
    }

    private fun open(context: Context, intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun createSignUri(cell: Cell, publicKey: PublicKeyEd25519): Uri {
        val boc = BagOfCells(cell)
        return createSignUri(boc, publicKey)
    }

    fun createSignUri(boc: BagOfCells, publicKey: PublicKeyEd25519): Uri {
        val body = hex(boc.toByteArray())
        return createSignUri(body, publicKey)
    }

    fun createSignUri(boc: String, publicKey: PublicKeyEd25519): Uri {
        val url = "tonsign://v1/?network=ton&pk=${publicKey.hex()}&body=$boc"
        return Uri.parse(url)
    }
}