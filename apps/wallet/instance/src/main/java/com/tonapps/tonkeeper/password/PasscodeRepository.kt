package com.tonapps.tonkeeper.password

import android.content.Context
import androidx.biometric.BiometricPrompt
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class PasscodeRepository(
    private val dataStore: PasscodeDataStore,
    private val settingsRepository: SettingsRepository,
) {

    val hasPinCode: Boolean
        get() = dataStore.hasPinCode

    suspend fun clear() {
        dataStore.clearPinCode()
    }

    suspend fun set(code: String) {
        dataStore.setPinCode(code)
    }

    suspend fun change(oldCode: String, newCode: String) = dataStore.change(oldCode, newCode)

    suspend fun compare(code: String) = dataStore.compare(code)

    suspend fun confirmation(context: Context): Boolean = withContext(Dispatchers.Main) {
        if (!settingsRepository.biometric || !PasscodeBiometric.isAvailableOnDevice(context)) {
            return@withContext dialog(context)
        }

        if (!biometric(context)) {
            dialog(context)
        } else {
            true
        }
    }

    fun confirmationFlow(context: Context) = flow {
        val valid = confirmation(context)
        if (!valid) {
            throw Exception("failed to request passcode")
        } else {
            emit(Unit)
        }
    }

    private suspend fun biometric(context: Context): Boolean = suspendCancellableCoroutine { continuation ->
        PasscodeBiometric.showPrompt(context, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                continuation.resume(false)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                continuation.resume(true)
            }
        })
    }

    private suspend fun dialog(context: Context): Boolean = suspendCancellableCoroutine { continuation ->
        val dialog = PasscodeDialog(context) { result ->
            continuation.resume(result)
        }
        dialog.show()
    }
}