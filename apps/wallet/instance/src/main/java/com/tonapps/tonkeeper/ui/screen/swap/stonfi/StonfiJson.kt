package com.tonapps.tonkeeper.ui.screen.swap.stonfi

import android.net.Uri
import com.tonapps.blockchain.Coin
import com.tonapps.blockchain.ton.extensions.toRawAddress
import com.tonapps.tonkeeper.App
import com.tonapps.tonkeeper.core.toDisplayAmount
import com.tonapps.tonkeeper.ui.screen.swap.data.AssetEntity
import com.tonapps.tonkeeper.ui.screen.swap.data.AssetKind
import com.tonapps.tonkeeper.ui.screen.swap.data.AssetTag
import com.tonapps.tonkeeper.ui.screen.swap.data.FormattedDecimal
import com.tonapps.tonkeeper.ui.screen.swap.data.SimulationAddresses
import com.tonapps.tonkeeper.ui.screen.swap.data.SimulationEntity
import com.tonapps.tonkeeper.ui.screen.swap.data.SimulationNumber
import com.tonapps.tonkeeper.ui.screen.swap.data.toUsdString
import com.tonapps.wallet.api.entity.TokenEntity
import org.json.JSONObject
import java.math.BigDecimal

fun JSONObject.toAssetEntity(): AssetEntity {
    val isBlacklisted = optBoolean("blacklisted")
    val tags = optJSONArray("tags")?.let {
        val list = mutableListOf<String>()
        for (index in 0 until it.length()) {
            list.add(it.getString(index))
        }
        list.toList()
    } ?: listOf()
    val verification = if (isBlacklisted) {
        TokenEntity.Verification.blacklist
    } else if (tags.contains("whitelisted")) {
        TokenEntity.Verification.whitelist
    } else {
        TokenEntity.Verification.none
    }

    val contractAddress = getString("contract_address")
    val kind = AssetKind.valueOf(getString("kind"))

    val token = if (kind == AssetKind.TON && verification == TokenEntity.Verification.whitelist && contractAddress == TokenEntity.TON_CONTRACT_USER_FRIENDLY_ADDRESS) {
        TokenEntity.TON
    } else {
        TokenEntity(
            address = contractAddress.toRawAddress(),
            name = getString("display_name"),
            symbol = getString("symbol"),
            imageUri = optString("image_url").let { imageUrl ->
                if (imageUrl.isNotEmpty()) {
                    Uri.parse(imageUrl)
                } else {
                    Uri.EMPTY
                }
            },
            decimals = getInt("decimals"),
            verification = verification
        )
    }

    val walletAddress = optString("wallet_address")
    val balanceRaw = optString("balance")
    val balanceCoins: BigDecimal? = balanceRaw.takeIf { it.isNotEmpty() }?.let {
        try {
            Coin.toCoins(balanceRaw, token.decimals)
        } catch (e: Exception) {
            null
        }
    }
    val balance = balanceCoins?.let {
        FormattedDecimal(it, it.toDisplayAmount(App.defaultNumberFormat(), token.decimals))
    }

    val dexPriceUsdString = optString("dex_price_usd") ?: optString("dex_usd_price")
    val thirdPartyPriceUsdString = optString("third_party_usd_price") ?: optString("third_party_price_usd")

    val dexPriceUsd = try {
        dexPriceUsdString.toBigDecimal()
    } catch (e: Exception) {
        null
    }
    val thirdPartyPriceUsd = try {
        thirdPartyPriceUsdString.toBigDecimal()
    } catch (e: Exception) {
        null
    }

    // TODO(API): return price in user currency to avoid extra requests
    val balanceUsd = (dexPriceUsd ?: thirdPartyPriceUsd)?.let {
        if (balance != null) {
            val balanceInUsd = it * balance.number
            FormattedDecimal(balanceInUsd, balanceInUsd.toUsdString())
        } else {
            null
        }
    }

    return AssetEntity(
        userFriendlyAddress = contractAddress,
        token = token,
        priority = getInt("priority"),
        kind = kind,
        balance = balance,
        deprecated = optBoolean("deprecated"),
        community = optBoolean("community"),
        blacklisted = isBlacklisted,
        defaultSymbol = optBoolean("default_symbol"),
        defaultList = optBoolean("default_list"),
        tags = AssetTag.fromTagsList(tags),
        dexPriceUsd = dexPriceUsd,
        thirdPartyPriceUsd = thirdPartyPriceUsd,
        balanceInUsd = balanceUsd,

        userCurrency = null, // TODO(API): see comment above
        priceInUserCurrency = null,
        balanceInUserCurrency = null
    )
}

const val STONFI_DISPLAY_NAME = "STON.fi"

fun JSONObject.toSimulationEntity(send: TokenEntity, receive: TokenEntity, isReverse: Boolean): SimulationEntity {
    val offerAddress: String = getString("offer_address")
    val askAddress: String = getString("ask_address")
    val routerAddress: String = getString("router_address")
    val poolAddress: String = getString("pool_address")
    val offerUnits: String = getString("offer_units")
    val askUnits: String = getString("ask_units")
    val slippageTolerance: String = getString("slippage_tolerance")
    val minAskUnits: String = getString("min_ask_units")
    val swapRate: String = getString("swap_rate")
    val priceImpact: String = getString("price_impact")
    val feeAddress: String = getString("fee_address")
    val feeUnits: String = getString("fee_units")
    val feePercent: String = getString("fee_percent")

    return SimulationEntity(
        SimulationAddresses(
            offerAddress = offerAddress,
            askAddress = askAddress,
            routerAddress = routerAddress,
            poolAddress = poolAddress,
            feeAddress = feeAddress
        ),
        send = SimulationNumber(offerUnits, send.decimals),
        receive = SimulationNumber(askUnits, receive.decimals),
        minReceived = SimulationNumber(minAskUnits, receive.decimals),
        swapRate = BigDecimal(swapRate).stripTrailingZeros(),
        priceImpact = BigDecimal(priceImpact).stripTrailingZeros(),
        fee = SimulationNumber(feeUnits, receive.decimals),
        slippageTolerance = BigDecimal(slippageTolerance),
        providerName = STONFI_DISPLAY_NAME
    )
}
