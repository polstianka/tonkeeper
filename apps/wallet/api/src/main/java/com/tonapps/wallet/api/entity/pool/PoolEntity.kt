package com.tonapps.wallet.api.entity.pool

import android.net.Uri
import android.os.Parcelable
import io.tonapi.models.PoolInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class PoolEntity(
    val address: String,
    val name: String,
    val totalAmount: Long,
    val implementation: PoolImplementationEntity,
    val apy: java.math.BigDecimal,
    val minStake: Long,
    val cycleStart: Long,
    val cycleEnd: Long,
    val verified: Boolean,
    val currentNominators: Int,
    val maxNominators: Int,
    val nominatorsStake: Long,
    val validatorStake: Long,
    val liquidJettonMaster: String? = null,
    val cycleLength: Long? = null
): Parcelable {
    constructor(info: PoolInfo, impl: PoolImplementationEntity) : this(
        address = info.address,
        name = info.name,
        totalAmount = info.totalAmount,
        implementation = impl,
        apy = info.apy,
        minStake = info.minStake,
        cycleStart = info.cycleStart,
        cycleEnd = info.cycleEnd,
        verified = info.verified,
        currentNominators = info.currentNominators,
        maxNominators = info.maxNominators,
        nominatorsStake = info.nominatorsStake,
        validatorStake = info.validatorStake,
        liquidJettonMaster = info.liquidJettonMaster,
        cycleLength = info.cycleLength
    )

    val links: List<Uri> get() = buildList {
        add(implementation.url)
        addAll(implementation.socials)
        add(Uri.parse("https://tonviewer.com/" + address))
    }
}
