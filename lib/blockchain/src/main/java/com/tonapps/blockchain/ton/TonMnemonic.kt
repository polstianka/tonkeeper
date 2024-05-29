package com.tonapps.blockchain.ton

import org.ton.mnemonic.Mnemonic

object TonMnemonic {

    private val mnemonicDividers = listOf(" ", "\n", ",", ";")
    private val mnemonicWords = Mnemonic.mnemonicWords()

    fun isValid(word: String?): Boolean {
        if (word.isNullOrBlank()) {
            return false
        }
        return mnemonicWords.contains(word)
    }

    fun isValid(list: List<String>): Boolean {
        return list.all { mnemonicWords.contains(it) }
    }

    fun parseMnemonic(value: String): List<String> {
        if (3 >= value.length) {
            return emptyList()
        }
        for (divider in mnemonicDividers) {
            if (value.contains(divider)) {
                return value.split(divider).filter { it.isNotBlank() }.map {
                    it.trim()
                }.map {
                    it.removePrefix(",").removeSuffix(",")
                }.map {
                    it.removePrefix(";").removeSuffix(";")
                }
            }
        }
        return emptyList()
    }
}