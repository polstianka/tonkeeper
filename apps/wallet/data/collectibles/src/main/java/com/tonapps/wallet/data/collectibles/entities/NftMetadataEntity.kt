package com.tonapps.wallet.data.collectibles.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NftMetadataEntity(
    val strings: HashMap<String, String>,
    val buttons: List<Button>
): Parcelable {

    @Parcelize
    data class Button(
        val label: String,
        val uri: String
    ): Parcelable {

        constructor(map: Map<String, String>) : this(
            label = map["label"] ?: "",
            uri = map["uri"] ?: ""
        )
    }

    val name: String?
        get() = strings["name"]

    val description: String?
        get() = strings["description"]

    val lottie: String?
        get() = strings["lottie"]

    constructor(map: Map<String, Any>) : this(
        strings = map.filter { it.value is String }.mapValues { it.value as String } as HashMap<String, String>,
        buttons = map["buttons"]?.let { buttons ->
            (buttons as List<Map<String, String>>).map {
                Button(it)
            }
        } ?: arrayListOf()
    )
}