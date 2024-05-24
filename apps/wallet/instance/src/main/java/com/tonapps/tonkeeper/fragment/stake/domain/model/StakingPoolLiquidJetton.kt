package com.tonapps.tonkeeper.fragment.stake.domain.model

import android.net.Uri
import com.tonapps.wallet.data.core.WalletCurrency
import java.math.BigDecimal

data class StakingPoolLiquidJetton(
    val address: String,
    val iconUri: Uri,
    val symbol: String,
    val price: BigDecimal?,
    val poolName: String,
    val currency: WalletCurrency
)
