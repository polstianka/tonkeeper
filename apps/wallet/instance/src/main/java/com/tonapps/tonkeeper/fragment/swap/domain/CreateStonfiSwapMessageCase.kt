package com.tonapps.tonkeeper.fragment.swap.domain

import com.tonapps.blockchain.ton.tlb.JettonTransfer
import com.tonapps.tonkeeper.core.toCoins
import com.tonapps.tonkeeper.extensions.sendToBlockchain
import com.tonapps.tonkeeper.fragment.send.TransactionData
import com.tonapps.tonkeeper.fragment.stake.domain.CreateWalletTransferCase
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetType
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSimulation
import com.tonapps.tonkeeper.fragment.swap.domain.model.getRecommendedGasValues
import com.tonapps.tonkeeper.fragment.swap.domain.model.recommendedForwardTon
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.legacy.WalletLegacy
import com.tonapps.wallet.data.account.legacy.WalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ton.block.MsgAddressInt
import org.ton.cell.buildCell
import org.ton.tlb.storeTlb
import java.math.BigDecimal

class CreateStonfiSwapMessageCase(
    private val createSwapCellCase: CreateSwapCellCase,
    private val createWalletTransferCase: CreateWalletTransferCase,
    private val api: API,
    private val walletManager: WalletManager,
    private val jettonWalletAddressRepository: JettonWalletAddressRepository
) {

    companion object {
        private const val ADDRESS_ROUTER = "EQB3ncyBUTjZUA5EnFKR5_EnOMI9V1tTEAAPaiU71gc4TiUt"
        private const val ADDRESS_ROUTER_TESTNET =
            "EQBsGx9ArADUrREB34W-ghgsCgBShvfUr4Jvlu-0KGc33Rbt"
        private const val ADDRESS_TON_PROXY =
            "0:8cdc1d7640ad5ee326527fc1ad0514f468b30dc84b0173f0e155f451b4e11f7c"
    }

    suspend fun execute(
        sendAsset: DexAsset,
        receiveAsset: DexAsset,
        offerAmount: BigDecimal,
        walletLegacy: WalletLegacy,
        simulation: SwapSimulation.Result
    ) = withContext(Dispatchers.IO) {
        val userWalletAddress = walletLegacy.address

        val minAskAmount = simulation.minimumReceivedAmount.toCoins(receiveAsset.decimals)

        val queryId = TransactionData.getWalletQueryId()
        val jettonToWalletAddress = getJettonToWalletAddress(
            sendAsset,
            receiveAsset,
            walletLegacy.testnet
        )
        val forwardAmount = sendAsset.type.recommendedForwardTon(receiveAsset.type)
        val attachedAmount = getAttachedAmount(sendAsset, receiveAsset, offerAmount)
        val swapCell = createSwapCellCase.execute(
            jettonToWalletAddress,
            minAskAmount,
            MsgAddressInt.parse(userWalletAddress)
        )
        val jettonTransferData = JettonTransfer(
            queryId = queryId,
            coins = offerAmount.toCoins(sendAsset.decimals),
            MsgAddressInt.parse(getRouterAddress(walletLegacy.testnet)),
            responseAddress = walletLegacy.contract.address,
            forwardAmount = forwardAmount.toCoins(),
            forwardPayload = swapCell
        )
        val jettonFromWalletAddressString = getJettonFromWalletAddress(
            sendAsset,
            receiveAsset,
            walletLegacy
        )
        val jettonFromWalletAddress = MsgAddressInt.parse(jettonFromWalletAddressString)
        val walletTransfer = createWalletTransferCase.execute(
            walletLegacy,
            jettonFromWalletAddress,
            attachedAmount,
            buildCell { storeTlb(JettonTransfer.tlbCodec(), jettonTransferData) }
        )
        val privateKey = walletManager.getPrivateKey(walletLegacy.id)
        walletLegacy.sendToBlockchain(api, privateKey, walletTransfer)
    }

    private suspend fun getJettonFromWalletAddress(
        sendAsset: DexAsset,
        receiveAsset: DexAsset,
        walletLegacy: WalletLegacy
    ): String {
        val (a, b) = when {
            sendAsset.type == DexAssetType.TON && receiveAsset.type == DexAssetType.JETTON ->
                ADDRESS_TON_PROXY to getRouterAddress(walletLegacy.testnet)

            sendAsset.type == DexAssetType.JETTON && receiveAsset.type == DexAssetType.TON ->
                sendAsset.contractAddress to walletLegacy.address

            sendAsset.type == DexAssetType.JETTON && receiveAsset.type == DexAssetType.JETTON ->
                sendAsset.contractAddress to walletLegacy.address

            else -> throw IllegalStateException("${sendAsset.type} -> ${receiveAsset.type}")
        }
        return jettonWalletAddressRepository.getJettonAddress(a, b)
    }

    private fun getAttachedAmount(
        sendAsset: DexAsset,
        receiveAsset: DexAsset,
        offerAmount: BigDecimal
    ): BigDecimal {
        return if (sendAsset.type == DexAssetType.TON && receiveAsset.type == DexAssetType.JETTON) {
            offerAmount + sendAsset.type.recommendedForwardTon(receiveAsset.type)
        } else {
            sendAsset.getRecommendedGasValues(receiveAsset)
        }
    }

    private suspend fun getJettonToWalletAddress(
        sendAsset: DexAsset,
        receiveAsset: DexAsset,
        testnet: Boolean
    ): String {
        val (a, b) = when {
            sendAsset.type == DexAssetType.TON && receiveAsset.type == DexAssetType.JETTON ->
                receiveAsset.contractAddress to getRouterAddress(testnet)

            sendAsset.type == DexAssetType.JETTON && receiveAsset.type == DexAssetType.TON ->
                ADDRESS_TON_PROXY to getRouterAddress(testnet)

            sendAsset.type == DexAssetType.JETTON && receiveAsset.type == DexAssetType.JETTON ->
                receiveAsset.contractAddress to getRouterAddress(testnet)

            else -> throw IllegalStateException("${sendAsset.type} -> ${receiveAsset.type}")
        }
        return jettonWalletAddressRepository.getJettonAddress(a, b)
    }

    private fun getRouterAddress(testnet: Boolean): String {
        return if (testnet) {
            ADDRESS_ROUTER_TESTNET
        } else {
            ADDRESS_ROUTER
        }
    }
}