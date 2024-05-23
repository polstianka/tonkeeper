package com.tonapps.tonkeeper.fragment.stake.confirm

import com.tonapps.blockchain.Coin
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.core.TextWrapper
import com.tonapps.tonkeeper.extensions.formatRate
import com.tonapps.tonkeeper.fragment.stake.confirm.rv.ConfirmStakeItemType
import com.tonapps.tonkeeper.fragment.stake.confirm.rv.ConfirmStakeListItem
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.withdrawalFee
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.rates.entity.RatesEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigDecimal

class ConfirmStakeListHelper(
    private val mapper: ConfirmStakeListItemMapper
) {

    private val _items = MutableStateFlow(listOf<ConfirmStakeListItem>())
    val items: Flow<List<ConfirmStakeListItem>>
        get() = _items

    fun init(walletEntity: WalletEntity, pool: StakingPool) {
        _items.value = ConfirmStakeItemType.entries.map { mapper.map(it, walletEntity, pool) }
    }

    fun setFee(
        feeLong: Long,
        rate: RatesEntity,
        pool: StakingPool
    ) {
        val feeBigDecimal = BigDecimal(feeLong).movePointLeft(Coin.TON_DECIMALS)
        val totalFee = feeBigDecimal + pool.serviceType.withdrawalFee
        val state = _items.value.toMutableList()
        val iterator = state.listIterator()
        while (iterator.hasNext()) {
            val current = iterator.next()
            if (current.itemType == ConfirmStakeItemType.FEE) {
                val amountCrypto = CurrencyFormatter.format(
                    "TON",
                    totalFee
                )
                val textCrypto = "~ $amountCrypto"
                val wrapperCrypto = TextWrapper.PlainString(textCrypto)
                val textFiat = "~ ${formatRate(rate, totalFee, "TON")}"
                val updatedItem = current.copy(
                    textPrimary = wrapperCrypto,
                    textSecondary = textFiat
                )
                iterator.set(updatedItem)
            }
        }
        _items.value = state
    }
}