package com.tonapps.tonkeeper.core.signer

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import org.ton.api.pub.PublicKeyEd25519
import org.ton.cell.Cell

class SignerResultContract : ActivityResultContract<SignerResultContract.Input, String?>() {

    data class Input(val body: Cell, val publicKey: PublicKeyEd25519)

    override fun createIntent(context: Context, input: Input): Intent {
        val uri = SignerApp.createSignUri(input.body, input.publicKey)
        return Intent(Intent.ACTION_SEND, uri)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode == Activity.RESULT_OK) {
            var boc = intent?.getStringExtra("boc")
            if (boc == null) {
                boc = intent?.data?.getQueryParameter("boc")
            }
            return boc
        }
        return null
    }
}