package com.tonapps.tonkeeper.fragment.send

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.tonapps.wallet.localization.Localization
import com.tonapps.tonkeeper.api.shortAddress
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import io.tonapi.models.JettonBalance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import uikit.extensions.textWithLabel
import uikit.mvi.UiFeature

class SendScreenFeature: UiFeature<SendScreenState, SendScreenEffect>(SendScreenState()) {

    private val _onReadyView: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val onReadyView = _onReadyView.asStateFlow()

    private val _transaction = MutableLiveData(TransactionData())
    val transaction: LiveData<TransactionData> = _transaction

    val transactionFlow = _transaction.asFlow()

    fun setBounce(bounce: Boolean) {
        _transaction.value = _transaction.value?.copy(bounce = bounce)
    }

    fun setAddress(address: String) {
        _transaction.value = _transaction.value?.copy(address = address)
    }

    fun setName(name: String?) {
        _transaction.value = _transaction.value?.copy(name = name)
    }

    fun setComment(comment: String?) {
        _transaction.value = _transaction.value?.copy(comment = comment)
    }

    fun toggleEncryptComment() {
        _transaction.value = _transaction.value?.copy(encryptComment = !_transaction.value?.encryptComment!!)
    }

    fun setAmount(amount: String) {
        _transaction.value = _transaction.value?.copy(amountRaw = amount)
    }

    fun setJetton(token: AccountTokenEntity?) {
        _transaction.value = _transaction.value?.copy(token = token)
    }

    fun setMax(max: Boolean) {
        _transaction.value = _transaction.value?.copy(max = max)
    }

    fun nextPage() {
        updateUiState { it.copy(currentPage = it.currentPage + 1) }
    }

    fun prevPage() {
        updateUiState { it.copy(currentPage = it.currentPage - 1) }
    }

    fun setCurrentPage(index: Int) {
        updateUiState { it.copy(currentPage = index) }
    }

    fun setHeaderTitle(title: String) {
        updateUiState { it.copy(headerTitle = title) }
    }

    fun setHeaderSubtitle(subtitle: CharSequence?) {
        updateUiState { it.copy(headerSubtitle = subtitle) }
    }

    fun setHeaderVisible(visibility: Boolean) {
        updateUiState { it.copy(headerVisible = visibility) }
    }

    fun setAmountHeader(context: Context) {
        setHeaderTitle(context.getString(Localization.amount))

        val address = _transaction.value?.address
        val name = _transaction.value?.name
        if (name.isNullOrEmpty()) {
            setHeaderSubtitle(address?.shortAddress)
        } else {
            setHeaderSubtitle(context.textWithLabel(name, address?.shortAddress))
        }
        setHeaderVisible(true)
    }

    fun readyView() {
        _onReadyView.value = true
    }
}