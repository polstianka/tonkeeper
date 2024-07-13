package com.tonapps.signer.screen.sign

import android.os.Bundle
import android.util.Log
import com.tonapps.blockchain.ton.extensions.base64
import com.tonapps.blockchain.ton.extensions.hex
import com.tonapps.blockchain.ton.extensions.parseCell
import com.tonapps.signer.Key
import com.tonapps.signer.deeplink.entities.ReturnResultEntity
import com.tonapps.signer.extensions.getObject
import org.ton.cell.Cell

data class SignArgs(private val args: Bundle) {

    companion object {

        fun bundle(
            id: Long,
            body: Cell,
            v: String,
            returnResult: ReturnResultEntity
        ) = Bundle().apply {
            putLong(Key.ID, id)
            putString(Key.V, v)
            putString(Key.BODY, body.hex())
            putParcelable(Key.RETURN, returnResult)
        }
    }

    val id = args.getLong(Key.ID)
    val body: Cell = args.getString(Key.BODY)!!.parseCell()
    val v: String = args.getString(Key.V)!!
    val returnResult = args.getObject<ReturnResultEntity>(Key.RETURN)

    val bodyHex: String by lazy { body.hex() }
}
