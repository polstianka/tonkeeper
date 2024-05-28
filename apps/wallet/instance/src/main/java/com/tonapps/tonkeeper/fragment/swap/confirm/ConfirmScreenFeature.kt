package com.tonapps.tonkeeper.fragment.swap.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.blockchain.Coin
import com.tonapps.blockchain.Coin.TON_DECIMALS
import com.tonapps.blockchain.ton.extensions.base64
import com.tonapps.blockchain.ton.extensions.toCell
import com.tonapps.blockchain.ton.tlb.JettonTransfer
import com.tonapps.blockchain.ton.tlb.SwapBody
import com.tonapps.tonkeeper.fragment.swap.StonfiConstants
import com.tonapps.tonkeeper.fragment.swap.currency.CurrencyScreenState
import com.tonapps.tonkeeper.fragment.swap.currency.list.SwapDetailsItem
import com.tonapps.tonkeeper.sign.RawMessageEntity
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ton.block.AddrNone
import org.ton.block.AddrStd
import org.ton.block.Coins
import org.ton.block.MsgAddressInt

class ConfirmScreenFeature(
    private val walletRepository: WalletRepository,
    private val api: API
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfirmScreenState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<ConfirmScreenEffect>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val uiEffect: SharedFlow<ConfirmScreenEffect> = _uiEffect.asSharedFlow()

    var resultState: CurrencyScreenState? = null

    fun confirm() = viewModelScope.launch(Dispatchers.IO) {
        _uiState.update { it.copy(loading = true) }

        val wallet = walletRepository.activeWalletFlow.firstOrNull() ?: throw Exception("wallet is null")

        val data = requireNotNull(resultState)

        val minAsk = data.details.items.filterIsInstance<SwapDetailsItem.Cell>()
            .first { it.title == Localization.minimum_received }
            .value
            .filter { it.isDigit() || it == '.' }
            .let { Coin.toNano(it.toFloat(), data.receiveInfo.token?.decimals ?: TON_DECIMALS) }
            .let { Coins.ofNano(it) }

        val queryId: Long = System.currentTimeMillis() / 1000
        val sendAmountNanos = Coin.toNano(data.sendInfo.amount, data.sendInfo.token!!.decimals)

        val rawMessage = when {
            data.sendInfo.token.isTon -> createBocTonJetton(
                queryId = queryId,
                sendAmountNanos = sendAmountNanos,
                data = data,
                wallet = wallet,
                minAsk = minAsk,
            )
            data.receiveInfo.token!!.isTon -> createBocJettonTon(
                queryId = queryId,
                sendAmountNanos = sendAmountNanos,
                data = data,
                wallet = wallet,
                minAsk = minAsk,
            )
            else -> createBocJettonJetton(
                queryId = queryId,
                sendAmountNanos = sendAmountNanos,
                data = data,
                wallet = wallet,
                minAsk = minAsk,
            )
        }

        val validUntil = ((System.currentTimeMillis() / 1000) + 1_000_000)
        val secretKey = walletRepository.getPrivateKey(wallet.id)
        val boc = walletRepository.createSignedMessage(
            wallet,
            secretKey,
            validUntil,
            listOf(rawMessage.walletTransfer)
        ).base64()

        val result = api.sendToBlockchain(boc, false)
        _uiState.emit(_uiState.value.copy(loading = false))
        delay(300)
        _uiEffect.emit(if (result) ConfirmScreenEffect.Success else ConfirmScreenEffect.Fail)
    }

    // ton -> jetton
    private fun createBocTonJetton(queryId: Long, sendAmountNanos: Long, data: CurrencyScreenState, wallet: WalletEntity, minAsk: Coins): RawMessageEntity {
        val fromWalletAddress = api.getWalletAddress(
            jettonMaster = StonfiConstants.TON_PROXY_ADDRESS,
            owner = StonfiConstants.ROUTER_ADDRESS,
            testnet = wallet.testnet
        )

        val toWalletAddress = api.getWalletAddress(
            jettonMaster = (MsgAddressInt(data.receiveInfo.token?.contractAddress.orEmpty()) as AddrStd).toString(userFriendly = false),
            owner = StonfiConstants.ROUTER_ADDRESS,
            testnet = wallet.testnet
        )!!

        val swapBody = SwapBody(
            askJettonWalletAddress = toWalletAddress,
            minAskAmount = minAsk,
            userWalletAddress = AddrStd(wallet.address),
            referralAddress = AddrStd(StonfiConstants.REFERRAL),
        )

        val transferMessage = JettonTransfer(
            queryId = queryId,
            amount = Coins.of(
                sendAmountNanos,
                data.sendInfo.token?.decimals ?: TON_DECIMALS
            ),
            destination = AddrStd(StonfiConstants.ROUTER_ADDRESS),
            responseDestination = AddrNone,
            customPayload = null,
            forwardTonAmount = Coins.ofNano(StonfiConstants.SWAP_TON_TO_JETTON.FORWARD_GAS_AMOUNT),
            forwardPayload = swapBody.toCell(),
        )

        val rawMessage = RawMessageEntity(
            addressValue = (fromWalletAddress as AddrStd).toString(userFriendly = true),
            amount = sendAmountNanos.plus(StonfiConstants.SWAP_TON_TO_JETTON.FORWARD_GAS_AMOUNT.toLong()),
            stateInitValue = "",
            payloadValue = transferMessage.toCell().base64(),
        )

        return rawMessage
    }

    // jetton -> jetton
    private fun createBocJettonJetton(queryId: Long, sendAmountNanos: Long, data: CurrencyScreenState, wallet: WalletEntity, minAsk: Coins): RawMessageEntity {
        val fromWalletAddress = api.getWalletAddress(
            jettonMaster = (MsgAddressInt(data.sendInfo.token?.contractAddress.orEmpty()) as AddrStd).toString(userFriendly = false),
            owner = AddrStd(wallet.address).toString(userFriendly = false),
            testnet = wallet.testnet
        )

        val toWalletAddress = api.getWalletAddress(
            jettonMaster = (MsgAddressInt(data.receiveInfo.token?.contractAddress.orEmpty()) as AddrStd).toString(userFriendly = false),
            owner = StonfiConstants.ROUTER_ADDRESS,
            testnet = wallet.testnet
        )!!

        val swapBody = SwapBody(
            askJettonWalletAddress = toWalletAddress,
            minAskAmount = minAsk,
            userWalletAddress = AddrStd(wallet.address),
            referralAddress = AddrStd(StonfiConstants.REFERRAL),
        )

        val transferMessage = JettonTransfer(
            queryId = queryId,
            amount = Coins.ofNano(sendAmountNanos),
            destination = AddrStd(StonfiConstants.ROUTER_ADDRESS),
            responseDestination = AddrStd(wallet.address),
            customPayload = null,
            forwardTonAmount = Coins.ofNano(StonfiConstants.SWAP_JETTON_TO_JETTON.FORWARD_GAS_AMOUNT),
            forwardPayload = swapBody.toCell(),
        )

        val rawMessage = RawMessageEntity(
            addressValue = (fromWalletAddress as AddrStd).toString(userFriendly = true),
            amount = StonfiConstants.SWAP_JETTON_TO_JETTON.GAS_AMOUNT.toLong(),
            stateInitValue = "",
            payloadValue = transferMessage.toCell().base64(),
        )

        return rawMessage
    }

    // jetton -> ton
    private fun createBocJettonTon(queryId: Long, sendAmountNanos: Long, data: CurrencyScreenState, wallet: WalletEntity, minAsk: Coins): RawMessageEntity {
        val fromWalletAddress = api.getWalletAddress(
            jettonMaster = (MsgAddressInt(data.sendInfo.token?.contractAddress.orEmpty()) as AddrStd).toString(userFriendly = false),
            owner = AddrStd(wallet.address).toString(userFriendly = false),
            testnet = wallet.testnet
        )!!

        val toWalletAddress = api.getWalletAddress(
            jettonMaster = StonfiConstants.TON_PROXY_ADDRESS,
            owner = StonfiConstants.ROUTER_ADDRESS,
            testnet = wallet.testnet
        )!!

        val swapBody = SwapBody(
            askJettonWalletAddress = toWalletAddress,
            minAskAmount = minAsk,
            userWalletAddress = AddrStd(wallet.address),
            referralAddress = AddrStd(StonfiConstants.REFERRAL),
        )

        val transferMessage = JettonTransfer(
            queryId = queryId,
            amount = Coins.ofNano(sendAmountNanos),
            destination = AddrStd(StonfiConstants.ROUTER_ADDRESS),
            responseDestination = AddrStd(wallet.address),
            customPayload = null,
            forwardTonAmount = Coins.ofNano(StonfiConstants.SWAP_JETTON_TO_TON.FORWARD_GAS_AMOUNT),
            forwardPayload = swapBody.toCell(),
        )

        val rawMessage = RawMessageEntity(
            addressValue = (fromWalletAddress as AddrStd).toString(userFriendly = true),
            amount = StonfiConstants.SWAP_JETTON_TO_TON.GAS_AMOUNT.toLong(),
            stateInitValue = "",
            payloadValue = transferMessage.toCell().base64(),
        )

        return rawMessage
    }

}