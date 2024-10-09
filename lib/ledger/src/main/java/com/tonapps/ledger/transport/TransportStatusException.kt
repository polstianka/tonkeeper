package com.tonapps.ledger.transport

sealed class TransportStatusException(message: String) : Exception(message) {
    class IncorrectLength : TransportStatusException("Incorrect length")
    class MissingCriticalParameter : TransportStatusException("Missing critical parameter")
    class SecurityNotSatisfied : TransportStatusException("Security not satisfied (dongle locked or have invalid access rights)")
    class DeniedByUser : TransportStatusException("Condition of use not satisfied (denied by the user?)")
    class InvalidDataReceived : TransportStatusException("Invalid data received")
    class InvalidParameterReceived : TransportStatusException("Invalid parameter received")
    class LockedDevice : TransportStatusException("Locked device")
    class BlindSigningDisabled : TransportStatusException("Blind signing is disabled")
    class InternalError : TransportStatusException("Internal error, please report")
    class UnknownError(code: Int) : TransportStatusException("Unknown error with status code: $code")

    companion object {
        fun fromStatusCode(statusCode: Int): TransportStatusException {
            return when (statusCode) {
                0x6700 -> IncorrectLength()
                0x6800 -> MissingCriticalParameter()
                0x6982 -> SecurityNotSatisfied()
                0x6985 -> DeniedByUser()
                0x6a80 -> InvalidDataReceived()
                0x6b00 -> InvalidParameterReceived()
                0x5515 -> LockedDevice()
                0xBD00 -> BlindSigningDisabled()
                in 0x6f00..0x6fff -> InternalError()
                else -> UnknownError(statusCode)
            }
        }
    }
}