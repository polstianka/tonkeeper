package com.tonapps.tonkeeper.ui.screen.swap

val NANOCOIN: Long = 1_000_000_000

fun Long.fromNanocoinToCoin(): Double {
    val stringValue = this.toString()
    return stringValue.padEnd(9, '0').toDouble() / NANOCOIN
}


fun Int.fromNanocoinToCoin(): Double {
    val stringValue = this.toString()
    return stringValue.padEnd(9, '0').toDouble() / NANOCOIN
}

fun Int.toNanoCoin(): Long {
    return this * NANOCOIN
}

fun Double.toNanoCoin(): Long {
    return (this * NANOCOIN).toLong()
}

fun Long.toNanoCoin(): Long {
    return this * NANOCOIN
}
