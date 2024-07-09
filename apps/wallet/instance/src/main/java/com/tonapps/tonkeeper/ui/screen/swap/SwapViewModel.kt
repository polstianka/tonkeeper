package com.tonapps.tonkeeper.ui.screen.swap

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.ui.screen.swap.view.inputItem.ProgressButtonState
import com.tonapps.tonkeeper.ui.screen.swap.model.assets.Asset
import com.tonapps.tonkeeper.ui.screen.swap.model.assets.SendTokenModel
import com.tonapps.tonkeeper.ui.screen.wallet.WalletViewModel
import com.tonapps.tonkeeper.ui.screen.wallet.list.Item
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.push.entities.AppPushEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.data.tonconnect.entities.DAppEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SwapViewModel(
    application: Application,
) : AndroidViewModel(application) {

    // StateEnterAmount
    // StateChooseToken
    // StateLoading
    // StateContinue
    // InsufficientBalance

    private var _stateProgressButton = MutableStateFlow<ProgressButtonState>(ProgressButtonState.StateEnterAmount)
    val stateProgressButton get () = _stateProgressButton


    fun updateStateProgressButton(newState: ProgressButtonState) {
        _stateProgressButton.update { newState }
    }

    private val vmContext = application
    private val swapRepository = SwapRepository(context = vmContext)

    private var _tokensList = MutableStateFlow<List<Asset>>(listOf())
    val tokenList get() = _tokensList


    private var _sendToken =
        MutableStateFlow<SendTokenModel>(SendTokenModel(token = null, sendValue = 0))
    val sendToken get() = _sendToken

    private var _receiveToken =
        MutableStateFlow<SendTokenModel>(SendTokenModel(token = null, sendValue = 0))
    val receiveToken get() = _receiveToken

    var clickState = false

    fun changeToken() {
        val savedSendToken = sendToken.value
        val savedReceiveToken = receiveToken.value
        sendToken.update { savedReceiveToken }
        receiveToken.update { savedSendToken }
    }

    fun updateSendToken(newToken: Asset) {
        if(newToken != sendToken.value.token) {
            _sendToken.update { it.copy(token = newToken, sendValue = 0)  }
        }
    }

    fun updateReceiveToken(newToken: Asset) {
        if(newToken != receiveToken.value.token) {
            _receiveToken.update { it.copy(token = newToken, sendValue = 0)  }
        }
    }

    fun getToken(address: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val repoResult = swapRepository.getTokenFormAccount(address)
            if (repoResult != null) {
                if (repoResult.asset_list != null) {
                    clickState = true
                    _sendToken.update {
                        SendTokenModel(
                            token = repoResult.asset_list[0],
                            it.sendValue
                        )
                    }
                    _tokensList.update { repoResult.asset_list }
                }
            }
        }
    }



    fun clearDataViewModel() {
        clickState = false
        tokenList.update { listOf() }
        sendToken.update { SendTokenModel(token = null, sendValue = 0) }
        receiveToken.update { SendTokenModel(token = null, sendValue = 0) }
    }
}