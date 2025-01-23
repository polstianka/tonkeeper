package com.tonapps.tonkeeper.manager.ledger

import android.util.Log
import com.ledger.live.ble.BleManager
import com.tonapps.ledger.ton.TonTransport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

fun BleManager.safeStopScanning() {
    try {
        stopScanning()
    } catch (ignored: Exception) {}
}

suspend fun TonTransport.safeIsAppOpened(): Boolean {
    return try {
        isAppOpen()
    } catch (e: Exception) {
        false
    }
}

suspend fun TonTransport.waitForAppOpen() = coroutineScope {
    while (isActive) {
        val isAppOpened = safeIsAppOpened()
        Log.d("LedgerBLEScan", "Is app opened: $isAppOpened")
        if (isAppOpened) {
            return@coroutineScope
        }
        delay(1000)
    }
}