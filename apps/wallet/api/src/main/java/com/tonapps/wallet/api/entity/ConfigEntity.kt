package com.tonapps.wallet.api.entity

import android.net.Uri
import android.os.Parcelable
import android.util.Log
import com.tonapps.extensions.toStringList
import com.tonapps.icu.Coins
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class ConfigEntity(
    val empty: Boolean,
    val supportLink: String,
    val nftExplorer: String,
    val transactionExplorer: String,
    val mercuryoSecret: String,
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
    val faqUrl: String,
    val aptabaseEndpoint: String,
    val aptabaseAppKey: String,
    val scamEndpoint: String,
    val batteryHost: String,
    val batteryTestnetHost: String,
    val batteryBeta: Boolean,
    val batteryDisabled: Boolean,
    val batterySendDisabled: Boolean,
    val batteryMeanFees: String,
    val batteryMeanPriceNft: String,
    val batteryMeanPriceSwap: String,
    val batteryMeanPriceJetton: String,
    val disableBatteryCryptoRecharge: Boolean,
    val disableBatteryIapModule: Boolean,
    val batteryReservedAmount: String,
    val batteryMaxInputAmount: String,
    val batteryRefundEndpoint: String,
    val batteryPromoDisable: Boolean,
    val stakingInfoUrl: String,
    val tonapiSSEEndpoint: String,
    val tonapiSSETestnetEndpoint: String,
    val iapPackages: List<IAPPackageEntity>,
    val burnZeroDomain: String,
    val scamAPIURL: String,
    val reportAmount: Coins,
    val stories: List<String>,
    val apkDownloadUrl: String?,
    val apkName: AppVersion?,
    val holdersAppEndpoint: String,
    val holdersServiceEndpoint: String,
): Parcelable {

    @IgnoredOnParcel
    val swapUri: Uri
        get() = Uri.parse(stonfiUrl)

    @IgnoredOnParcel
    val isBatteryDisabled: Boolean
        get() = batteryDisabled || batterySendDisabled

    @IgnoredOnParcel
    val domains: List<String> by lazy {
        listOf(tonapiMainnetHost, tonapiTestnetHost, tonapiSSEEndpoint, tonapiSSETestnetEndpoint, "https://bridge.tonapi.io/", "https://tonapi.io/")
    }

    @IgnoredOnParcel
    val apk: ApkEntity? by lazy {
        val name = apkName ?: return@lazy null
        val url = apkDownloadUrl ?: return@lazy null
        ApkEntity(url, name)
    }

    constructor(json: JSONObject, debug: Boolean) : this(
        empty = false,
        supportLink = json.getString("supportLink"),
        nftExplorer = json.getString("NFTOnExplorerUrl"),
        transactionExplorer = json.getString("transactionExplorer"),
        mercuryoSecret = json.getString("mercuryoSecret"),
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
        flags = FlagsEntity(json.getJSONObject("flags")), /*if (debug) {
            FlagsEntity()
        } else {
            FlagsEntity(json.getJSONObject("flags"))
        },*/
        faqUrl = json.getString("faq_url"),
        aptabaseEndpoint = json.getString("aptabaseEndpoint"),
        aptabaseAppKey = json.getString("aptabaseAppKey"),
        scamEndpoint = json.optString("scamEndpoint", "https://scam.tonkeeper.com"),
        batteryHost = json.optString("batteryHost", "https://battery.tonkeeper.com"),
        batteryTestnetHost = json.optString("batteryTestnetHost", "https://testnet-battery.tonkeeper.com"),
        batteryBeta = json.optBoolean("battery_beta", true),
        batteryDisabled = json.optBoolean("disable_battery", false),
        batterySendDisabled = json.optBoolean("disable_battery_send", false),
        batteryMeanFees = json.optString("batteryMeanFees", "0.0055"),
        disableBatteryCryptoRecharge = json.optBoolean("disable_battery_crypto_recharge_module", false),
        disableBatteryIapModule = json.optBoolean("disable_battery_iap_module", false),
        batteryMeanPriceNft = json.optString("batteryMeanPrice_nft", "0.03"),
        batteryMeanPriceSwap = json.optString("batteryMeanPrice_swap", "0.22"),
        batteryMeanPriceJetton = json.optString("batteryMeanPrice_jetton", "0.06"),
        batteryReservedAmount = json.optString("batteryReservedAmount", "0.3"),
        batteryMaxInputAmount = json.optString("batteryMaxInputAmount", "3"),
        batteryRefundEndpoint = json.optString("batteryRefundEndpoint", "https://battery-refund-app.vercel.app"),
        batteryPromoDisable = json.optBoolean("disable_battery_promo_module", true),
        stakingInfoUrl = json.getString("stakingInfoUrl"),
        tonapiSSEEndpoint = json.optString("tonapi_sse_endpoint", "https://rt.tonapi.io"),
        tonapiSSETestnetEndpoint = json.optString("tonapi_sse_testnet_endpoint", "https://rt-testnet.tonapi.io"),
        iapPackages = json.optJSONArray("iap_packages")?.let { array ->
            (0 until array.length()).map { IAPPackageEntity(array.getJSONObject(it)) }
        } ?: emptyList(),
        burnZeroDomain = json.optString("burnZeroDomain", "UQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAJKZ"), // tonkeeper-zero.ton
        scamAPIURL = json.optString("scam_api_url", "https://scam.tonkeeper.com"),
        reportAmount = Coins.of(json.optString("reportAmount") ?: "0.03"),
        stories = json.getJSONArray("stories").toStringList(),
        apkDownloadUrl = json.optString("apk_download_url"),
        apkName = json.optString("apk_name")?.let { AppVersion(it) },
        holdersAppEndpoint = json.optString("holdersAppEndpoint", "https://app.holders.io"),
        holdersServiceEndpoint = json.optString("holdersServiceEndpoint", "https://card-prod.whales-api.com")
    )

    constructor() : this(
        empty = true,
        supportLink = "mailto:support@tonkeeper.com",
        nftExplorer = "https://tonviewer.com/nft/%s",
        transactionExplorer = "https://tonviewer.com/transaction/%s",
        mercuryoSecret = "",
        tonapiMainnetHost = "https://keeper.tonapi.io",
        tonapiTestnetHost = "https://testnet.tonapi.io",
        stonfiUrl = "https://tonkeeper.ston.fi/swap",
        tonNFTsMarketplaceEndpoint = "https://ton.diamonds",
        directSupportUrl = "https://t.me/tonkeeper_supportbot",
        tonkeeperNewsUrl = "https://t.me/tonkeeper_new",
        tonCommunityUrl = "https://t.me/toncoin",
        tonCommunityChatUrl = "https://t.me/toncoin_chat",
        tonApiV2Key = "",
        featuredPlayInterval = 3000,
        flags = FlagsEntity(),
        faqUrl = "https://tonkeeper.helpscoutdocs.com/",
        aptabaseEndpoint = "https://anonymous-analytics.tonkeeper.com",
        aptabaseAppKey = "A-SH-4314447490",
        scamEndpoint = "https://scam.tonkeeper.com",
        batteryHost = "https://battery.tonkeeper.com",
        batteryTestnetHost = "https://testnet-battery.tonkeeper.com",
        batteryBeta = true,
        batteryDisabled = false,
        batterySendDisabled = false,
        batteryMeanFees = "0.0055",
        disableBatteryCryptoRecharge = false,
        disableBatteryIapModule = false,
        batteryMeanPriceNft = "0.03",
        batteryMeanPriceSwap = "0.22",
        batteryMeanPriceJetton = "0.06",
        batteryReservedAmount = "0.3",
        batteryMaxInputAmount = "3",
        batteryRefundEndpoint = "https://battery-refund-app.vercel.app",
        batteryPromoDisable = true,
        stakingInfoUrl = "https://ton.org/stake",
        tonapiSSEEndpoint = "https://rt.tonapi.io",
        tonapiSSETestnetEndpoint = "https://rt-testnet.tonapi.io",
        iapPackages = emptyList(),
        burnZeroDomain = "UQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAJKZ",
        scamAPIURL = "https://scam.tonkeeper.com",
        reportAmount = Coins.of("0.03"),
        stories = emptyList(),
        apkDownloadUrl = null,
        apkName = null,
        holdersAppEndpoint = "https://app.holders.io",
        holdersServiceEndpoint = "https://card-prod.whales-api.com"
    )

    companion object {
        val default = ConfigEntity()
    }
}