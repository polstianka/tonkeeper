package com.tonapps.signer.screen.root.action

import com.tonapps.signer.deeplink.entities.ReturnResultEntity
import org.ton.api.pub.PublicKeyEd25519
import org.ton.cell.Cell

sealed class RootAction {
    data class RequestBodySign(
        val id: Long,
        val body: Cell,
        val v: String,
        val returnResult: ReturnResultEntity
    ): RootAction()

    data class ResponseSignature(val signature: ByteArray): RootAction() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ResponseSignature

            return signature.contentEquals(other.signature)
        }

        override fun hashCode(): Int {
            return signature.contentHashCode()
        }

    }

    data class ResponseKey(val publicKey: PublicKeyEd25519, val name: String): RootAction()

    data object UpdateApp: RootAction()
}