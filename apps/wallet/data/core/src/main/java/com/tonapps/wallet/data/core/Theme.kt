package com.tonapps.wallet.data.core

data class Theme(
    val key: String,
    val resId: Int,
    val light: Boolean
) {

    companion object {

        private val supportedThemes = mutableListOf<Theme>()

        fun getByKey(key: String): Theme {
            return supportedThemes.firstOrNull { it.key == key } ?: supportedThemes.first()
        }

        fun getByResId(resId: Int): Theme {
            return supportedThemes.firstOrNull { it.resId == resId } ?: supportedThemes.first()
        }

        fun add(key: String, resId: Int, light: Boolean = false) {
            supportedThemes.add(Theme(key, resId, light))
        }

        fun getSupported(): List<Theme> {
            return supportedThemes
        }


    }
}