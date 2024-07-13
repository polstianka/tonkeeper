package com.tonapps.tonkeeper.password

import android.content.Context
import core.keyvalue.EncryptedKeyValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PasscodeDataStore(context: Context) {

    companion object {
        const val CODE_LENGTH = 4

        private const val NAME = "passcode"
        private const val CODE_KEY = "code"
    }

    private val keyValue = EncryptedKeyValue(context, NAME)

    val hasPinCode: Boolean
        get() = keyValue.contains(CODE_KEY)

    suspend fun setPinCode(code: String) {
        keyValue.putString(CODE_KEY, code)
    }

    suspend fun clearPinCode() {
        keyValue.remove(CODE_KEY)
    }

    suspend fun change(oldCode: String, newCode: String): Boolean {
        if (compare(oldCode)) {
            setPinCode(newCode)
            return true
        }
        return false
    }

    suspend fun compare(code: String): Boolean = withContext(Dispatchers.IO) {
        val savedCode = keyValue.getString(CODE_KEY)
        savedCode == code
    }
}