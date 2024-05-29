package com.tonapps.tonkeeper.ui.screen.swap.data

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class PairMap(
    val pairs: List<Pair<String, String>> = emptyList()
) : Parcelable {
    @IgnoredOnParcel
    private val cache: MutableMap<String, MutableSet<String>> by lazy {
        mutableMapOf()
    }

    fun isEmpty(): Boolean = pairs.isEmpty()
    fun isNotEmpty(): Boolean = pairs.isNotEmpty()

    operator fun get(key: String): Set<String> {
        val set = cache[key]
        if (set == null) {
            val mutableSet = mutableSetOf<String>()
            for (pair in pairs) {
                if (pair.first == key) {
                    mutableSet += pair.second
                } else if (pair.second == key) {
                    mutableSet += pair.first
                }
            }
            cache[key] = mutableSet
            return mutableSet
        } else {
            return set
        }
    }
}