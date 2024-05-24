package com.tonapps.signer.deeplink.entities

import android.net.Uri
import android.util.Log
import com.tonapps.blockchain.ton.extensions.safeParseCell
import com.tonapps.blockchain.ton.extensions.safePublicKey
import com.tonapps.signer.Key
import org.ton.api.pub.PublicKeyEd25519
import org.ton.cell.Cell

data class SignRequestEntity(
    val uri: Uri,
    val returnResult: ReturnResultEntity
) {

    companion object {

        fun safe(uri: Uri, returnResult: ReturnResultEntity): SignRequestEntity? {
            return try {
                SignRequestEntity(uri, returnResult)
            } catch (e: Throwable) {
                null
            }
        }
    }

    val body: Cell = uri.getQueryParameter(Key.BODY)?.safeParseCell() ?: throw IllegalArgumentException("body is required")
    val publicKey: PublicKeyEd25519 = uri.getQueryParameter(Key.PK)?.safePublicKey() ?: throw IllegalArgumentException("pk is required")

    // val network: String? = uri.getQueryParameter("network")
    val v: String = uri.getQueryParameter(Key.V) ?: "v4r2"
}