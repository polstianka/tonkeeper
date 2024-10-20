package com.tonapps.blockchain.ton.extensions

import org.ton.bitstring.BitString
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.cell.CellSlice
import org.ton.crypto.hex

fun String.toBoc(): BagOfCells {
    return try {
        val fixedBoc = this.replace("-", "+")
            .replace("_", "/")
        BagOfCells(fixedBoc.base64())
    } catch (e: Throwable) {
        BagOfCells(hex(this))
    }
}

fun String.parseCell(): Cell {
    return toBoc().first()
}

fun String.safeParseCell(): Cell? {
    if (this.isBlank()) {
        return null
    }
    return try {
        parseCell()
    } catch (e: Throwable) {
        null
    }
}

fun Cell.toByteArray(): ByteArray {
    return BagOfCells(this).toByteArray()
}

fun Cell.base64(): String {
    return org.ton.crypto.base64(toByteArray())
}

fun Cell.hex(): String {
    return hex(toByteArray())
}

fun CellSlice.loadRemainingBits(): BitString {
    return BitString((this.bitsPosition until this.bits.size).map { this.loadBit() })
}
