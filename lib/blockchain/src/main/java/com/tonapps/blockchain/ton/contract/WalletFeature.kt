package com.tonapps.blockchain.ton.contract

@JvmInline
value class WalletFeature(private val flag: Int) {

    companion object {
        val NONE = WalletFeature(0)
        val GASLESS = WalletFeature(1)
        val SIGNED_INTERNALS = WalletFeature(2)
    }

    infix fun and(other: WalletFeature): WalletFeature = WalletFeature(this.flag and other.flag)
    infix fun or(other: WalletFeature): WalletFeature = WalletFeature(this.flag or other.flag)

    operator fun contains(feature: WalletFeature): Boolean = (this.flag and feature.flag) == feature.flag
}