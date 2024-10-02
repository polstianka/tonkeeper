package com.tonapps.wallet.api.entity

enum class IAPPackageId(val id: String) {
    LARGE("large"),
    MEDIUM("medium"),
    SMALL("small");

    companion object {
        fun fromId(id: String): IAPPackageId {
            return entries.find { it.id.equals(id, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid IAPPackageId id: $id")
        }
    }
}
