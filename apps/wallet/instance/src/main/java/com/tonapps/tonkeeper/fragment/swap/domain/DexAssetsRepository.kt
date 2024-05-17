package com.tonapps.tonkeeper.fragment.swap.domain

import com.tonapps.tonkeeper.fragment.swap.domain.model.AssetBalance
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAssetType
import com.tonapps.wallet.api.StonfiAPI
import io.stonfiapi.models.AssetInfoSchema
import io.stonfiapi.models.AssetKindSchema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class DexAssetsRepository(
    private val api: StonfiAPI
) {

    private val _isLoading = MutableStateFlow(false)
    private val _communityItems = MutableStateFlow<List<DexAsset>>(emptyList())
    private val _nonCommunityItems = MutableStateFlow<List<DexAsset>>(emptyList())

    val communityItems: StateFlow<List<DexAsset>>
        get() = _communityItems
    val nonCommunityItems: StateFlow<List<DexAsset>>
        get() = _nonCommunityItems
    val isLoading: StateFlow<Boolean>
        get() = _isLoading

    suspend fun loadAssets() = withContext(Dispatchers.IO) {
        _isLoading.value = true
        val response = api.dex.getAssetList()
        val community = mutableListOf<AssetInfoSchema>()
        val notCommunity = mutableListOf<AssetInfoSchema>()
        response.assetList.forEach { asset ->
            when {
                asset.blacklisted -> Unit
                asset.deprecated -> Unit
                asset.community -> community.add(asset)
                else -> notCommunity.add(asset)
            }
        }
        _communityItems.value = community.map { it.toDomain() }
        _nonCommunityItems.value = notCommunity.map { it.toDomain() }

        _isLoading.value = false
    }

    fun getDefaultAsset(): DexAsset {
        return nonCommunityItems.value.first { it.type == DexAssetType.TON }
    }


    private val assetBalances = mutableListOf<AssetBalance.Entity>()
    fun getAssetBalance(
        walletAddress: String,
        contractAddress: String
    ) = flow {
        if (assetBalances.isEmpty()) {
            emit(AssetBalance.Loading)
            val assets = withContext(Dispatchers.IO) {
                api.wallets.getWalletAssets(walletAddress)
                    .assetList
                    .map { AssetBalance.Entity(it.toDomain(), it.balance?.toLongOrNull() ?: 0L) }
            }
            assetBalances.clear()
            assetBalances.addAll(assets)
        }
        val balance = assetBalances.firstOrNull { it.asset.contractAddress == contractAddress }
        emit(balance)
    }

    private fun AssetInfoSchema.toDomain(): DexAsset {
        return DexAsset(
            isCommunity = community,
            contractAddress = contractAddress,
            decimals = decimals,
            hasDefaultSymbol = defaultSymbol,
            type = kind.toDomain(),
            symbol = symbol,
            imageUrl = imageUrl!!,
            displayName = displayName!!
        )
    }

    private fun AssetKindSchema.toDomain(): DexAssetType {
        return when (this) {
            AssetKindSchema.Jetton -> DexAssetType.JETTON
            AssetKindSchema.Wton -> DexAssetType.WTON
            AssetKindSchema.Ton -> DexAssetType.TON
        }
    }
}