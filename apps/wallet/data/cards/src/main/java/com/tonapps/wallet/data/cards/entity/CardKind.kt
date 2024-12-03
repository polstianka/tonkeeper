package com.tonapps.wallet.data.cards.entity

enum class CardKind(val kind: String) {
    VIRTIAL("virtual"),
    UNKNOWN("unknown");

    companion object {
        fun fromString(id: String): CardKind {
            return entries.find { it.kind.equals(id, ignoreCase = true) } ?: UNKNOWN
        }
    }
}