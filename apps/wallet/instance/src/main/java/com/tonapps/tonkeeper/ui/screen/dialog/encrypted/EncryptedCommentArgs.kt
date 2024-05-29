package com.tonapps.tonkeeper.ui.screen.dialog.encrypted

import android.os.Bundle
import uikit.base.BaseArgs

data class EncryptedCommentArgs(
    val cipherText: String,
    val senderAddress: String
): BaseArgs() {

    private companion object {
        private const val CIPHER_TEXT_ARG = "cipher_text"
        private const val SENDER_ADDRESS_ARG = "sender_address"
    }

    constructor(bundle: Bundle) : this(
        cipherText = bundle.getString(CIPHER_TEXT_ARG)!!,
        senderAddress = bundle.getString(SENDER_ADDRESS_ARG)!!
    )

    override fun toBundle(): Bundle = Bundle().apply {
        putString(CIPHER_TEXT_ARG, cipherText)
        putString(SENDER_ADDRESS_ARG, senderAddress)
    }
}