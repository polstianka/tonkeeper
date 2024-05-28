package com.tonapps.wallet.api.entity

import android.net.Uri
import android.os.Parcelable
import android.util.Log
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class ConfigEntity(
    val supportLink: String,
    val nftExplorer: String,
    val transactionExplorer: String,
    val mercuryoSecret: String,
    val stonfiHost: String,
    val tonapiMainnetHost: String,
    val tonapiTestnetHost: String,
    val stonfiUrl: String,
    val tonNFTsMarketplaceEndpoint: String,
    val directSupportUrl: String,
    val tonkeeperNewsUrl: String,
    val tonCommunityUrl: String,
    val tonCommunityChatUrl: String,
    val tonApiV2Key: String,
    val featuredPlayInterval: Int,
    val flags: FlagsEntity,
    val faqUrl: String
): Parcelable {

    val swapUri: Uri
        get() = Uri.parse(stonfiUrl)

    constructor(json: JSONObject, debug: Boolean) : this(
        supportLink = json.getString("supportLink"),
        nftExplorer = json.getString("NFTOnExplorerUrl"),
        transactionExplorer = json.getString("transactionExplorer"),
        mercuryoSecret = json.getString("mercuryoSecret"),
        stonfiHost = json.getString("stonfiHost"),
        tonapiMainnetHost = json.getString("tonapiMainnetHost"),
        tonapiTestnetHost = json.getString("tonapiTestnetHost"),
        stonfiUrl = json.getString("stonfiUrl"),
        tonNFTsMarketplaceEndpoint = json.getString("tonNFTsMarketplaceEndpoint"),
        directSupportUrl = json.getString("directSupportUrl"),
        tonkeeperNewsUrl = json.getString("tonkeeperNewsUrl"),
        tonCommunityUrl = json.getString("tonCommunityUrl"),
        tonCommunityChatUrl = json.getString("tonCommunityChatUrl"),
        tonApiV2Key = json.getString("tonApiV2Key"),
        featuredPlayInterval = json.optInt("featured_play_interval", 3000),
        flags = if (debug) {
            FlagsEntity()
        } else {
            FlagsEntity(json.getJSONObject("flags"))
        },
        faqUrl = json.getString("faq_url")
    )

    constructor() : this(
        supportLink = "mailto:support@tonkeeper.com",
        nftExplorer = "https://tonviewer.com/nft/%s",
        transactionExplorer = "https://tonviewer.com/transaction/%s",
        mercuryoSecret = "",
        stonfiHost = "https://api.ston.fi",
        tonapiMainnetHost = "https://keeper.tonapi.io",
        tonapiTestnetHost = "https://testnet.tonapi.io",
        stonfiUrl = "https://tonkeeper.ston.fi/swap",
        tonNFTsMarketplaceEndpoint = "https://ton.diamonds",
        directSupportUrl = "https://t.me/tonkeeper_supportbot",
        tonkeeperNewsUrl = "https://t.me/tonkeeper_new",
        tonCommunityUrl = "https://t.me/toncoin",
        tonCommunityChatUrl = "https://t.me/toncoin_chat",
        tonApiV2Key = "AF77F5JND26OLHQAAAAKQMSCYW3UVPFRA7CF2XHX6QG4M5WAMF5QRS24R7J4TF2UTSXOZEY",
        featuredPlayInterval = 3000,
        flags = FlagsEntity(),
        faqUrl = "https://tonkeeper.helpscoutdocs.com/"
    )

    companion object {
        val default = ConfigEntity()
    }
}