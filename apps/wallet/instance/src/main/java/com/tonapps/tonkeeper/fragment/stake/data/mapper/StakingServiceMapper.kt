package com.tonapps.tonkeeper.fragment.stake.data.mapper

import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingServiceType
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingSocial
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingSocialType
import io.tonapi.models.PoolImplementation
import io.tonapi.models.PoolImplementationType
import io.tonapi.models.PoolInfo
import java.net.URI

class StakingServiceMapper {

    fun map(
        pools: Map.Entry<PoolImplementationType, MutableSet<PoolInfo>>,
        implementations: Map<String, PoolImplementation>
    ): StakingService {
        val type = pools.key.toDomain()
        val implementation = implementations.entries.first { it.key == pools.key.value }.value
        return StakingService(
            type = type,
            pools = pools.value.asSequence()
                .map { it.toDomain(type) }
                .sortedByDescending { it.apy }
                .toList(),
            description = implementation.description,
            name = implementation.name,
            socials = implementation.getSocials()
        )
    }

    private fun PoolImplementation.getSocials(): List<StakingSocial> {
        val url = StakingSocial(StakingSocialType.LINK, url)
        val result = mutableListOf(url)
        socials.forEach { link ->
            result.add(StakingSocial(link.socialType(), link))
        }
        return result
    }

    private fun String.socialType(): StakingSocialType {
        val uri = URI.create(this)
        val host = uri.host
        return when {
            host.startsWith("twitter.com") ||
                    host.startsWith("x.com") -> StakingSocialType.TWITTER

            host.startsWith("t.me") -> StakingSocialType.TELEGRAM

            else -> StakingSocialType.LINK
        }
    }

    private fun PoolInfo.toDomain(stakingServiceType: StakingServiceType): StakingPool {
        return StakingPool(
            address = address,
            apy = apy,
            currentNominators = currentNominators,
            cycleEnd = cycleEnd,
            cycleLength = cycleLength,
            cycleStart = cycleStart,
            serviceType = stakingServiceType,
            liquidJettonMaster = liquidJettonMaster,
            maxNominators = maxNominators,
            minStake = minStake,
            name = name,
            nominatorsStake = nominatorsStake,
            totalAmount = totalAmount,
            validatorStake = validatorStake,
            isMaxApy = false
        )
    }

    private fun PoolImplementationType.toDomain(): StakingServiceType {
        return when (this) {
            PoolImplementationType.whales -> StakingServiceType.WHALES
            PoolImplementationType.tf -> StakingServiceType.TF
            PoolImplementationType.liquidTF -> StakingServiceType.LIQUID_TF
        }
    }
}