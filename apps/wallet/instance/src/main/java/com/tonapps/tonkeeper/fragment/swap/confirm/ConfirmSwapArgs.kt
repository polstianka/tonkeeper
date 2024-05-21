package com.tonapps.tonkeeper.fragment.swap.confirm

import android.os.Bundle
import com.tonapps.tonkeeper.fragment.swap.domain.model.DexAsset
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSettings
import com.tonapps.tonkeeper.fragment.swap.domain.model.SwapSimulation
import com.tonapps.tonkeeper.fragment.swap.domain.model.toBundle
import com.tonapps.tonkeeper.fragment.swap.domain.model.toSwapSettings
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.entity.RatesEntity
import uikit.base.BaseArgs
import java.math.BigDecimal

data class ConfirmSwapArgs(
    val sendAsset: DexAsset,
    val receiveAsset: DexAsset,
    val settings: SwapSettings,
    val amount: BigDecimal,
    val simulation: SwapSimulation.Result,
    val currency: WalletCurrency,
    val ratesEntity: RatesEntity,
    val ratesUsd: RatesEntity
) : BaseArgs() {

    companion object {
        private const val KEY_SEND_ASSET = "KEY_SEND_ASSET "
        private const val KEY_RECEIVE_ASSET = "KEY_RECEIVE_ASSET"
        private const val KEY_SETTINGS = "KEY_SETTINGS"
        private const val KEY_AMOUNT = "KEY_AMOUNT"
        private const val KEY_SIMULATION = "KEY_SIMULATION"
        private const val KEY_CURRENCY = "KEY_CURRENCY "
        private const val KEY_RATES = "KEY_RATES "
        private const val KEY_RATES_USD = "KEY_RATES_USD "
    }

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putParcelable(KEY_SEND_ASSET, sendAsset)
            putParcelable(KEY_RECEIVE_ASSET, receiveAsset)
            putBundle(KEY_SETTINGS, settings.toBundle())
            putSerializable(KEY_AMOUNT, amount)
            putParcelable(KEY_SIMULATION, simulation)
            putParcelable(KEY_CURRENCY, currency)
            putParcelable(KEY_RATES, ratesEntity)
            putParcelable(KEY_RATES_USD, ratesUsd)
        }
    }

    constructor(bundle: Bundle) : this(
        sendAsset = bundle.getParcelable(KEY_SEND_ASSET)!!,
        receiveAsset = bundle.getParcelable(KEY_RECEIVE_ASSET)!!,
        settings = bundle.getBundle(KEY_SETTINGS)!!.toSwapSettings(),
        amount = bundle.getSerializable(KEY_AMOUNT) as BigDecimal,
        simulation = bundle.getParcelable(KEY_SIMULATION)!!,
        currency = bundle.getParcelable(KEY_CURRENCY)!!,
        ratesEntity = bundle.getParcelable(KEY_RATES)!!,
        ratesUsd = bundle.getParcelable(KEY_RATES_USD)!!
    )
}