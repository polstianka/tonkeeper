package com.tonapps.tonkeeper.core.signer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import org.ton.api.pub.PublicKeyEd25519
import org.ton.cell.Cell
import org.ton.crypto.hex

class SingerResultContract : ActivityResultContract<SingerResultContract.Input, ByteArray?>() {

    data class Input(val body: Cell, val publicKey: PublicKeyEd25519)

    override fun createIntent(context: Context, input: Input): Intent {
        val uri = SignerApp.createSignUri(input.body, input.publicKey)
        return Intent(Intent.ACTION_SEND, uri)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): ByteArray? {
        if (resultCode == Activity.RESULT_OK) {
            var sign = intent?.getStringExtra("sign")
            if (sign == null) {
                sign = intent?.data?.getQueryParameter("sign") ?: return null
            }
            return hex(sign)
        }
        return null
    }
}