package com.tonapps.wallet.data.token.entities

import android.net.Uri
import android.os.Parcelable
import com.tonapps.wallet.api.entity.BalanceEntity
import com.tonapps.wallet.api.entity.TokenEntity
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class AccountTokenEntity(
    val balance: BalanceEntity,
    @IgnoredOnParcel
    var rate: TokenRateEntity? = null
): Parcelable {

    val imageUri: Uri
        get() = balance.token.imageUri

    val address: String
        get() = balance.token.address

    val decimals: Int
        get() = balance.token.decimals

    val name: String
        get() = balance.token.name

    val symbol: String
        get() = balance.token.symbol

    val isTon: Boolean
        get() = address == "TON"

    val fiat: BigDecimal
        get() = rate?.fiat ?: BigDecimal.ZERO

    val rateNow: BigDecimal
        get() = rate?.rate ?: BigDecimal.ZERO

    val rateDiff24h: String
        get() = rate?.rateDiff24h ?: ""

    val verified: Boolean
        get() = balance.token.verification == TokenEntity.Verification.whitelist
}