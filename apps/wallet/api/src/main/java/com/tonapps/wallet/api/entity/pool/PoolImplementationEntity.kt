package com.tonapps.wallet.api.entity.pool

import android.net.Uri
import android.os.Parcelable
import com.tonapps.wallet.api.R
import io.tonapi.models.PoolImplementation
import io.tonapi.models.PoolImplementationType
import kotlinx.parcelize.Parcelize

@Parcelize
data class PoolImplementationEntity(
    val type: PoolImplementationType,
    val name: String,
    val description: String,
    val url: Uri,
    val socials: List<Uri>,
): Parcelable {
    constructor(type: PoolImplementationType, info: PoolImplementation) : this(
        type = type,
        name = info.name,
        description = info.description,
        url = Uri.parse(info.url),
        socials = info.socials.map { Uri.parse(it) }
    )
}
