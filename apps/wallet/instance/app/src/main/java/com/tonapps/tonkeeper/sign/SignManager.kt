package com.tonapps.tonkeeper.sign

import android.os.CancellationSignal
import com.tonapps.blockchain.ton.extensions.EmptyPrivateKeyEd25519
import com.tonapps.tonkeeper.core.AnalyticsHelper
import com.tonapps.tonkeeper.core.history.HistoryHelper
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.extensions.toastLoading
import com.tonapps.tonkeeper.ui.screen.action.ActionScreen
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.account.AccountRepository
import com.tonapps.wallet.data.battery.BatteryRepository
import com.tonapps.wallet.data.battery.entity.BatterySupportedTransaction
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.core.entity.SignRequestEntity
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.localization.Localization
import kotlinx.coroutines.suspendCancellableCoroutine
import uikit.navigation.Navigation
import java.lang.IllegalArgumentException
import kotlin.coroutines.resume

class SignManager(
    private val ratesRepository: RatesRepository,
    private val settingsRepository: SettingsRepository,
    private val accountRepository: AccountRepository,
    private val api: API,
    private val historyHelper: HistoryHelper,
    private val batteryRepository: BatteryRepository,
) {

    suspend fun action(
        navigation: Navigation,
        wallet: WalletEntity,
        request: SignRequestEntity,
        canceller: CancellationSignal = CancellationSignal(),
        batteryTxType: BatterySupportedTransaction? = null,
    ): String {
        navigation.toastLoading(true)
        var isBattery = batteryTxType != null && batteryRepository.isSupportedTransaction(wallet.accountId, batteryTxType)
        val details: HistoryHelper.Details?
        if (isBattery) {
            val result = emulateBattery(request, wallet)
            details = result.first
            isBattery = result.second
        } else {
            details = emulate(request, wallet)
        }
        navigation.toastLoading(false)

        if (details == null) {
            navigation.toast(Localization.error)
            throw IllegalArgumentException("Failed to emulate")
        }

        val boc = getBoc(navigation, wallet, request, details, canceller, isBattery) ?: throw IllegalArgumentException("Failed boc")
        AnalyticsHelper.trackEvent("send_transaction")
        val success = if (isBattery) batteryRepository.sendToRelayer(wallet, boc) else api.sendToBlockchain(boc, wallet.testnet)
        if (success) {
            AnalyticsHelper.trackEvent("send_success")
        } else {
            throw Exception("Failed to send transaction")
        }
        return boc
    }

    private suspend fun getBoc(
        navigation: Navigation,
        wallet: WalletEntity,
        request: SignRequestEntity,
        details: HistoryHelper.Details,
        canceller: CancellationSignal,
        isBattery: Boolean,
    ) = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { canceller.cancel() }

        val requestKey = "sign_request"
        navigation.setFragmentResultListener(requestKey) { bundle ->
            if (continuation.isActive) {
                continuation.resume(ActionScreen.parseResult(bundle))
            }
        }
        navigation.add(ActionScreen.newInstance(details, wallet.id, request, requestKey, isBattery))
    }

    private suspend fun emulate(
        request: SignRequestEntity,
        wallet: WalletEntity,
        currency: WalletCurrency = settingsRepository.currency,
    ): HistoryHelper.Details? {
        val rates = ratesRepository.getRates(currency, "TON")
        val seqno = accountRepository.getSeqno(wallet)
        val cell = accountRepository.createSignedMessage(wallet, seqno, EmptyPrivateKeyEd25519, request.validUntil, request.transfers)
        return try {
            val emulated = api.emulate(cell, wallet.testnet) ?: return null
            historyHelper.create(wallet, emulated, rates)
        } catch (e: Throwable) {
            null
        }
    }

    private suspend fun emulateBattery(
        request: SignRequestEntity,
        wallet: WalletEntity,
        currency: WalletCurrency = settingsRepository.currency,
    ): Pair<HistoryHelper.Details?, Boolean> {
        val rates = ratesRepository.getRates(currency, "TON")
        val seqno = accountRepository.getSeqno(wallet)
        val cell = accountRepository.createSignedMessage(wallet, seqno, EmptyPrivateKeyEd25519, request.validUntil, request.transfers, internalMessage = true)
        return try {
            val response = batteryRepository.emulate(wallet, cell)
            Pair(historyHelper.create(wallet, response.consequences, rates, isBattery = true), response.withBattery)
        } catch (e: Throwable) {
            Pair(emulate(request, wallet, currency), false)
        }
    }
}