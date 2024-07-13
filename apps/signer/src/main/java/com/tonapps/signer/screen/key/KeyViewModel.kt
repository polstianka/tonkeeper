package com.tonapps.signer.screen.key

import android.content.Context
import androidx.lifecycle.ViewModel
import com.tonapps.signer.core.repository.KeyRepository
import com.tonapps.signer.password.Password
import com.tonapps.signer.vault.SignerVault
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.take
import com.tonapps.security.vault.safeArea

class KeyViewModel(
    private val id: Long,
    private val keyRepository: KeyRepository,
    private val vault: SignerVault,
): ViewModel() {

    val keyEntity = keyRepository.getKey(id).filterNotNull()

    fun delete(context: Context) = Password.authenticate(context).safeArea {
        vault.delete(it, id)
        val size = keyRepository.deleteKey(id)
        if (size == 0) {
            vault.clear()
        }
    }.take(1)

    fun getRecoveryPhrase(context: Context) = Password.authenticate(context).safeArea {
        vault.getMnemonic(it, id).toTypedArray()
    }.take(1)
}