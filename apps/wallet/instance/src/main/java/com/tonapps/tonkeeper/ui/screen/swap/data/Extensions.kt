package com.tonapps.tonkeeper.ui.screen.swap.data

import android.util.Log
import com.tonapps.blockchain.ton.extensions.EmptyPrivateKeyEd25519
import com.tonapps.blockchain.ton.extensions.toUserFriendly
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.icu.CurrencyFormatter.preferredDisplayDecimalCount
import com.tonapps.tonkeeper.ui.screen.swap.stonfi.Stonfi
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletRepository
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import kotlinx.datetime.Clock
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.bigint.BigInt
import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.block.StateInit
import org.ton.cell.Cell
import org.ton.contract.wallet.WalletTransfer
import org.ton.contract.wallet.WalletTransferBuilder
import ton.SendMode
import ton.transfer.QueryId
import ton.transfer.Transfer
import java.math.BigDecimal
import kotlin.time.Duration.Companion.minutes

fun TokenEntity?.isSameToken(other: TokenEntity?): Boolean {
    return (this == null && other == null) || (
        this != null && other != null && this.address == other.address
    )
}

fun TokenEntity?.isSameToken(other: AccountTokenEntity?): Boolean {
    return (this == null && other == null) || (
        this != null && other != null && this.address == other.address
    )
}

fun BigDecimal.toCurrencyString(currency: WalletCurrency, decimals: Int = -1): String {
    return CurrencyFormatter.format(currency.code, this, decimals).toString()
}

fun BigDecimal.toUsdString(decimals: Int = -1): String {
    return CurrencyFormatter.format(WalletCurrency.USD_CODE, this, decimals).toString()
}

data class CurrencyFormatPair(
    val first: String,
    val second: String,
    val decimals: Int
)

fun BigDecimal.createPair(currency: WalletCurrency, other: BigDecimal, maxAttemptsCount: Int = -1): CurrencyFormatPair? {
    val a = this.stripTrailingZeros()
    val b = other.stripTrailingZeros()

    val aStart = a.preferredDisplayDecimalCount()
    val bStart = b.preferredDisplayDecimalCount()
    val startDecimalCount = minOf(
        aStart,
        bStart
    )

    var updatedAFormat: String
    var updatedBFormat: String
    var attemptsCount = 0
    var attemptDecimalCount = 0
    var updated: Boolean
    do {
        attemptsCount++
        attemptDecimalCount = startDecimalCount + attemptsCount
        updatedAFormat = a.toCurrencyString(currency, maxOf(aStart, attemptDecimalCount))
        updatedBFormat = b.toCurrencyString(currency, maxOf(bStart, attemptDecimalCount))
        updated = updatedAFormat != updatedBFormat
    } while (!updated && (maxAttemptsCount == -1 || attemptsCount < maxAttemptsCount))
    if (updated) {
        return CurrencyFormatPair(updatedAFormat, updatedBFormat, attemptDecimalCount)
    }
    return null
}

fun SwapRequest.prepareSwapTransferDetails(repository: WalletRepository,
                                           wallet: WalletEntity, sendMaximum: Boolean = false,
                                           referralAddress: String? = null): SwapRequestTransferDetails {
    val testnet = wallet.testnet
    val userWalletAddress = wallet.address

    val tonProxyAddress = Stonfi.TONProxyAddress.toUserFriendly(false, testnet)
    val simulationData = confirmedSimulation.simulation.data!!
    if (Stonfi.RouterAddress.toUserFriendly(false, testnet) != simulationData.addresses.routerAddress) {
        // TODO: remove this log message
        Log.e(SwapConfig.LOGGING_TAG, "[${SwapConfig.debugTimestamp()}] Received router_address that doesn't match the constant: ${simulationData.addresses.routerAddress.toUserFriendly(false, testnet)} vs ${Stonfi.RouterAddress.toUserFriendly(false, testnet)}")
    }
    val routerAddress = simulationData.addresses.routerAddress.toUserFriendly(false, testnet)

    val fromMasterAddress = sendAsset.token.getUserFriendlyAddress(false, testnet)
    val toMasterAddress = receiveAsset.token.getUserFriendlyAddress(false, testnet)

    val offerAmount = simulationData.send.nano
    val minAskAmount: BigInt = simulationData.minReceived.nano

    val fromWalletAddress: String
    val toWalletAddress: String
    val attachedAmount: BigInt
    val forwardAmount: BigInt

    // Adjust this field if "Blockchain fees" is incorrect.
    val userVisibleFeesAmount: BigInt = BigInt.ZERO

    when (this.type) {
        SwapRequest.Type.TON_TO_JETTON -> {
            fromWalletAddress = repository.walletAddress(
                jettonMaster = tonProxyAddress,
                owner = routerAddress
            )
            toWalletAddress = repository.walletAddress(
                jettonMaster = toMasterAddress,
                owner = routerAddress
            )
            forwardAmount = Stonfi.SWAP_TON_TO_JETTON.ForwardGasAmount
            attachedAmount = Stonfi.SWAP_TON_TO_JETTON.ForwardGasAmount + offerAmount
        }
        SwapRequest.Type.JETTON_TO_JETTON -> {
            fromWalletAddress = repository.walletAddress(
                jettonMaster = fromMasterAddress,
                owner = userWalletAddress
            )
            toWalletAddress = repository.walletAddress(
                jettonMaster = toMasterAddress,
                owner = routerAddress
            )
            forwardAmount = Stonfi.SWAP_JETTON_TO_JETTON.ForwardGasAmount
            attachedAmount = Stonfi.SWAP_JETTON_TO_JETTON.GasAmount
        }
        SwapRequest.Type.JETTON_TO_TON -> {
            fromWalletAddress = repository.walletAddress(
                jettonMaster = fromMasterAddress,
                owner = userWalletAddress
            )
            toWalletAddress = repository.walletAddress(
                jettonMaster = tonProxyAddress,
                owner = routerAddress
            )
            forwardAmount = Stonfi.SWAP_JETTON_TO_TON.ForwardGasAmount
            attachedAmount = Stonfi.SWAP_JETTON_TO_TON.GasAmount
        }
    }

    return SwapRequestTransferDetails(
        userWalletAddress = userWalletAddress,
        minAskAmount = minAskAmount,
        offerAmount = offerAmount,
        jettonToWalletAddress = toWalletAddress,
        jettonFromWalletAddress = fromWalletAddress,
        routerAddress = routerAddress,
        forwardAmount = forwardAmount,
        attachedAmount = attachedAmount,
        sendMaximum = sendMaximum,
        referralAddress = referralAddress,
        userVisibleFeesAmount = userVisibleFeesAmount
    )
}

fun SwapRequestTransferDetails.toWalletTransfer(
    init: StateInit?
): WalletTransfer {
    val queryId = QueryId.newQueryId()
    val stonfiSwapData = Transfer.swap(
        assetToSwap = MsgAddressInt.parse(jettonToWalletAddress),
        minAskAmount = Coins.ofNano(minAskAmount),
        userWalletAddress = MsgAddressInt.parse(userWalletAddress),
        referralAddress = referralAddress?.let {
            MsgAddressInt.parse(it)
        }
    )
    val jettonTransferData = Transfer.jetton(
        queryId = queryId,
        coins = Coins.ofNano(offerAmount),
        toAddress = MsgAddressInt.parse(routerAddress),
        responseAddress = MsgAddressInt.parse(userWalletAddress),
        forwardAmount = Coins.ofNano(forwardAmount),
        forwardPayload = stonfiSwapData
    )
    val walletTransfer = WalletTransferBuilder().apply {
        stateInit = init
        destination = MsgAddressInt.parse(jettonFromWalletAddress)
        coins = Coins.ofNano(attachedAmount)
        bounceable = true
        body = jettonTransferData
        sendMode = if (sendMaximum) {
            SendMode.CARRY_ALL_REMAINING_BALANCE.value
        } else {
            SendMode.PAY_GAS_SEPARATELY.value or SendMode.IGNORE_ERRORS.value
        }
    }.build()
    return walletTransfer
}

object SwapTimeout {
    fun generateValidUntilSeconds(): Long {
        return (Clock.System.now() + 15.minutes).epochSeconds // STON.fi: Date.now() + 1e6
    }
}

/*suspend fun SwapRequestTransferDetails.toUnsignedBodyCell(repository: WalletRepository, wallet: WalletEntity, knownSeqno: Int? = null, existingWalletTransfer: WalletTransfer? = null): Cell {
    val seqno = knownSeqno ?: repository.walletSeqno(wallet)
    val needStateInit = seqno == 0
    val walletTransfer = existingWalletTransfer ?: toWalletTransfer(wallet, needStateInit)
    val validUntilSeconds = SwapTimeout.generateValidUntilSeconds()

    return wallet.contract.createTransferUnsignedBody(
        seqno = seqno,
        validUntil = validUntilSeconds,
        gifts = arrayOf(walletTransfer)
    )
}

suspend fun SwapRequestTransferDetails.toSignRequestEntity(repository: WalletRepository, wallet: WalletEntity, knownSeqno: Int? = null, existingWalletTransfer: WalletTransfer? = null): SignRequestEntity {
    val seqno = knownSeqno ?: repository.walletSeqno(wallet)
    val needStateInit = seqno == 0
    val walletTransfer = existingWalletTransfer ?: toWalletTransfer(wallet, needStateInit)
    val validUntilSeconds = SwapTimeout.generateValidUntilSeconds()

    val message = RawMessageEntity(
        MsgAddressInt.toString(walletTransfer.destination, userFriendly = true),
        attachedAmount,
        walletTransfer.stateInit?.bocBase64(),
        walletTransfer.body!!.base64()
    )
    return SignRequestEntity(
        fromValue = wallet.address,
        validUntil = validUntilSeconds,
        listOf(message),
        if (wallet.testnet) TonNetwork.TESTNET else TonNetwork.MAINNET
    )
}*/

fun WalletEntity.sign(
    seqno: Int,
    privateKey: PrivateKeyEd25519 = EmptyPrivateKeyEd25519,
    vararg gifts: WalletTransfer
): Cell {
    val unsignedBody = contract.createTransferUnsignedBody(seqno = seqno, gifts = gifts)
    return contract.createTransferMessageCell(
        address = contract.address,
        privateKey = privateKey,
        seqno = seqno,
        unsignedBody = unsignedBody,
    )
}

fun Throwable.toUserVisibleMessage(): String { // Leaving an ability to return not string
    val message = this.message ?: ""
    return message.ifEmpty {
        javaClass.name
    }
}