package com.tonapps.wallet.data.token.entities


import android.os.Parcelable
import io.tonapi.models.Asset
import kotlinx.parcelize.Parcelize

@Parcelize
data class AssetEntity(

    /*val balance: kotlin.String,*/

    val blacklisted: Boolean,

    val community: Boolean,

    val contractAddress: kotlin.String,

    val decimals: Int,

    // val defaultSymbol: Boolean,

    val deprecated: Boolean,

    // val dexPriceUsd: String,

    // val dexUsdPrice: String,

    val displayName: String?,

    val imageUrl: String?,

    val symbol: String,

    // val walletAddress: kotlin.String,

    val swapableAssets: MutableList<String> = mutableListOf(),

): Parcelable {

    var balance : Double = 0.0

    var hiddenBalance : Boolean = false

    val isTon = contractAddress == tonContractAddress

    companion object {
        const val tonContractAddress = "EQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAM9c"
    }

    constructor(
        asset: Asset
    ) : this (
        /*asset.balance,*/
        asset.blacklisted,
        asset.community,
        asset.contractAddress,
        asset.decimals,
        //asset.defaultSymbol,
        asset.deprecated,
        //asset.dexPriceUsd,
        //asset.dexUsdPrice,
        asset.displayName,
        asset.imageUrl,
        asset.symbol,
        // asset.walletAddress
    )

}