package com.tonapps.wallet.data.rates.source

import android.content.Context
import com.tonapps.extensions.toByteArray
import com.tonapps.extensions.toParcel
import com.tonapps.wallet.data.core.BlobDataSource
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.entity.RateEntity
import com.tonapps.wallet.data.rates.entity.RatesEntity

class BlobDataSource(context: Context): BlobDataSource<RatesEntity>(
    context = context,
    path = "rates",
    lruInitialCapacity = 3
) {

    override fun onUnmarshall(bytes: ByteArray) = bytes.toParcel<RatesEntity>()

    override fun onMarshall(data: RatesEntity) = data.toByteArray()

    fun get(currency: WalletCurrency): RatesEntity {
        val rates = getCache(currency.code) ?: RatesEntity.empty(currency)
        if (rates.isEmpty) {
            clearCache(currency.code)
            return rates
        }
        return rates.copy()
    }

    fun add(currency: WalletCurrency, list: List<RateEntity>): Boolean {
        if (list.isEmpty()) {
            return false
        }
        val rates = get(currency).merge(list)
        return updateCache(currency.code, rates)
    }

}