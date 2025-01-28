package com.tonapps.ledger

sealed class LedgerException(msg: String, cause: Throwable? = null): Exception(msg, cause) {

    data object USBWriteException: LedgerException("Error sending data to Ledger device by USB.") {
        private fun readResolve(): Any = USBWriteException
    }

    data object USBReadException: LedgerException("Error reading data from Ledger device by USB.") {
        private fun readResolve(): Any = USBReadException
    }
}