package com.tonapps.wallet.data.settings

@JvmInline
value class BatteryTransaction(val code: Int) {

    companion object {
        val SWAP = BatteryTransaction(0)
        val JETTON = BatteryTransaction(1)
        val NFT = BatteryTransaction(2)

        val entries = arrayOf(SWAP, JETTON, NFT)

        fun Array<BatteryTransaction>.toIntArray(): IntArray {
            return map { it.code }.toIntArray()
        }

        fun List<BatteryTransaction>.toIntArray(): IntArray {
            return map { it.code }.toIntArray()
        }

    }

}