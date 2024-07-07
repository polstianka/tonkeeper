package com.tonapps.wallet.api.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class StakePoolsEntity(
    val pools: List<PoolInfo>,
    val implementations: Map<String, PoolImplementation>
) : Parcelable {
    @Parcelize
    data class PoolInfo(
        val address: String,
        val name: String,
        val totalAmount: Long,
        val implementation: PoolImplementationType,
        val apy: BigDecimal,
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
    ) : Parcelable

    @Parcelize
    data class PoolImplementation(
        val name: String,
        val description: String,
        val url: String,
        val socials: List<String>
    ) : Parcelable

    @Parcelize
    enum class PoolImplementationType(val value: String) : Parcelable {
        whales("whales"),
        tf("tf"),
        liquidTF("liquidTF");

        companion object {

            fun find(s: String): PoolImplementationType = entries.first { it.value == s }
        }
    }

    companion object {
        val Empty = StakePoolsEntity(emptyList(), emptyMap())
    }
}