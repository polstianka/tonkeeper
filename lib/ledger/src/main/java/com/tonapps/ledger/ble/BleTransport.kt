package com.tonapps.ledger.ble

import com.ledger.live.ble.BleManager
import com.tonapps.ledger.transport.Transport
import io.ktor.util.hex
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class BleTransport(private val bleManager: BleManager) : Transport {
    override suspend fun exchange(apdu: ByteArray): ByteArray {
        if (bleManager.isConnected) {
            return send(apdu)
        } else {
            throw IllegalStateException("BleTransport is not connected")
        }
    }

    private suspend fun send(apdu: ByteArray) = suspendCancellableCoroutine { continuation ->
        try {
            bleManager.send(
                apduHex = hex(apdu),
                onSuccess = { response ->
                    continuation.resume(hex(response))
                },
                onError = { error ->
                    continuation.resumeWithException(IllegalStateException(error))
                }
            )
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
}