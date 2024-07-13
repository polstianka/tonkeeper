package com.tonapps.signer.password

import android.content.Context
import com.tonapps.signer.password.ui.PasswordDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.isActive
import java.util.concurrent.atomic.AtomicLong
import javax.crypto.SecretKey

object Password {

    private const val MIN_LENGTH = 4
    private const val UNLOCK_TIMEOUT = 60000L

    private var unlockTime = AtomicLong(0)

    fun isValid(value: CharArray): Boolean {
        return value.size >= MIN_LENGTH
    }

    fun setUnlock() {
        val time = System.currentTimeMillis()
        unlockTime.set(time)
    }

    fun isUnlocked(): Boolean {
        val time = unlockTime.get()
        if (0 >= time) {
            return false
        }
        return time + UNLOCK_TIMEOUT > System.currentTimeMillis()
    }

    fun authenticate(context: Context): Flow<SecretKey> = callbackFlow {
        val dialog = PasswordDialog(context, ::trySend)
        dialog.setOnDismissListener {
            if (isActive) {
                close(UserCancelThrowable )
            } else {
                close()
            }
        }
        dialog.show()
        awaitClose { dialog.destroy() }
    }.flowOn(Dispatchers.Main).take(1)

    object UserCancelThrowable : Throwable("User canceled") {
        private fun readResolve(): Any = UserCancelThrowable
    }
}