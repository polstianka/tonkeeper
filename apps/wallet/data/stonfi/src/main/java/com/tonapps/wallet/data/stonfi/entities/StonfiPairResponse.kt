package com.tonapps.wallet.data.stonfi.entities

data class StonfiPairResponse(val pairs: List<List<String>>) {

    fun getPairs(): Map<String, List<String>> {
        val map = mutableMapOf<String, MutableList<String>>()
        for (pair in pairs) {
            val pairDefault = map[pair[0]]
            if (pairDefault == null) {
                map[pair[0]] = mutableListOf(pair[1])
            } else {
               pairDefault.add(pair[1])
            }


            val pairReverse = map[pair[1]]
            if (pairReverse == null) {
                map[pair[1]] = mutableListOf(pair[0])
            } else {
                pairReverse.add(pair[0])
            }
        }
        return map
    }
}

