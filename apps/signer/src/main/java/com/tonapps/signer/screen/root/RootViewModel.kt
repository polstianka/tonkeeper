package com.tonapps.signer.screen.root

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.signer.Key
import com.tonapps.signer.core.repository.KeyRepository
import com.tonapps.signer.deeplink.entities.ReturnResultEntity
import com.tonapps.signer.deeplink.entities.SignRequestEntity
import com.tonapps.signer.password.Password
import com.tonapps.signer.screen.root.action.RootAction
import com.tonapps.signer.vault.SignerVault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.ton.cell.Cell
import uikit.extensions.collectFlow

class RootViewModel(
    private val vault: SignerVault,
    private val keyRepository: KeyRepository
): ViewModel() {

    val hasKeys = keyRepository.stream.map {
        it.isNotEmpty()
    }.distinctUntilChanged()

    private val _action = MutableSharedFlow<RootAction>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val action = _action.asSharedFlow()

    fun checkPassword(password: CharArray) = flow {
        val valid = vault.isValidPassword(password)
        if (valid) {
            Password.setUnlock()
        }
        emit(valid)
    }.flowOn(Dispatchers.IO).take(1)

    fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            keyRepository.clear()
            vault.clear()
        }
    }

    fun responseSignedBoc(boc: String) {
        _action.tryEmit(RootAction.ResponseBoc(boc))
    }

    fun processDeepLink(uri: Uri, fromApp: Boolean): Boolean {
        if (uri.scheme != Key.SCHEME) {
            return false
        }
        if (uri.query.isNullOrEmpty()) { // if no query, then it's a just open app
            return true
        }

        val returnResult = ReturnResultEntity(fromApp, uri.getQueryParameter("return"))

        val signRequest = SignRequestEntity.safe(uri, returnResult) ?: return false

        signRequest(signRequest)
        return true
    }

    private fun signRequest(signRequest: SignRequestEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = keyRepository.findIdByPublicKey(signRequest.publicKey) ?: return@launch
            sign(id, signRequest.body, signRequest.v, signRequest.returnResult)
        }
    }

    private fun sign(id: Long, body: Cell, v: String, returnResult: ReturnResultEntity) {
        _action.tryEmit(RootAction.RequestBodySign(id, body, v, returnResult))
    }
}