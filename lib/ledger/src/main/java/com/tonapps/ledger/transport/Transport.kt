package com.tonapps.ledger.transport

import java.nio.ByteBuffer
import java.nio.ByteOrder

interface Transport {

    open fun close() {

    }

    suspend fun exchange(
        apdu: ByteArray,
    ): ByteArray

    suspend fun send(
        system: Int,
        command: Int,
        p1: Int,
        p2: Int,
        data: ByteArray = ByteArray(0),
        responseCodes: List<Int> = listOf(StatusCodes.OK)
    ): ByteArray {
        val buffer = ByteBuffer.allocate(5 + data.size)
        buffer.put(system.toByte())
        buffer.put(command.toByte())
        buffer.put(p1.toByte())
        buffer.put(p2.toByte())
        buffer.put(data.size.toByte())
        buffer.put(data)

        val response = exchange(buffer.array())

        val sw = readUInt16BE(response, response.size - 2)

        if (!responseCodes.contains(sw)) {
            throw TransportStatusException.fromStatusCode(sw)
        }
        
        return response
    }

    private fun readUInt16BE(response: ByteArray, offset: Int): Int {
        val buffer = ByteBuffer.wrap(response, offset, 2)
        buffer.order(ByteOrder.BIG_ENDIAN)
        return buffer.short.toInt() and 0xFFFF
    }

    companion object {
        object StatusCodes {
            const val ACCESS_CONDITION_NOT_FULFILLED = 0x9804
            const val ALGORITHM_NOT_SUPPORTED = 0x9484
            const val CLA_NOT_SUPPORTED = 0x6e00
            const val CODE_BLOCKED = 0x9840
            const val CODE_NOT_INITIALIZED = 0x9802
            const val COMMAND_INCOMPATIBLE_FILE_STRUCTURE = 0x6981
            const val CONDITIONS_OF_USE_NOT_SATISFIED = 0x6985
            const val CONTRADICTION_INVALIDATION = 0x9810
            const val CONTRADICTION_SECRET_CODE_STATUS = 0x9808
            const val CUSTOM_IMAGE_BOOTLOADER = 0x662f
            const val CUSTOM_IMAGE_EMPTY = 0x662e
            const val FILE_ALREADY_EXISTS = 0x6a89
            const val FILE_NOT_FOUND = 0x9404
            const val GP_AUTH_FAILED = 0x6300
            const val HALTED = 0x6faa
            const val INCONSISTENT_FILE = 0x9408
            const val INCORRECT_DATA = 0x6a80
            const val INCORRECT_LENGTH = 0x6700
            const val INCORRECT_P1_P2 = 0x6b00
            const val INS_NOT_SUPPORTED = 0x6d00
            const val DEVICE_NOT_ONBOARDED = 0x6d07
            const val DEVICE_NOT_ONBOARDED_2 = 0x6611
            const val INVALID_KCV = 0x9485
            const val INVALID_OFFSET = 0x9402
            const val LICENSING = 0x6f42
            const val LOCKED_DEVICE = 0x5515
            const val MAX_VALUE_REACHED = 0x9850
            const val MEMORY_PROBLEM = 0x9240
            const val MISSING_CRITICAL_PARAMETER = 0x6800
            const val NO_EF_SELECTED = 0x9400
            const val NOT_ENOUGH_MEMORY_SPACE = 0x6a84
            const val OK = 0x9000
            const val PIN_REMAINING_ATTEMPTS = 0x63c0
            const val REFERENCED_DATA_NOT_FOUND = 0x6a88
            const val SECURITY_STATUS_NOT_SATISFIED = 0x6982
            const val TECHNICAL_PROBLEM = 0x6f00
            const val UNKNOWN_APDU = 0x6d02
            const val USER_REFUSED_ON_DEVICE = 0x5501
            const val NOT_ENOUGH_SPACE = 0x5102
        }
    }
}
