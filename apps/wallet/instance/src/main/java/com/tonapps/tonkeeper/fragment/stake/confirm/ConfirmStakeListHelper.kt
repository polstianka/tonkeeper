package com.tonapps.tonkeeper.fragment.stake.confirm

import com.tonapps.tonkeeper.core.TextWrapper
import com.tonapps.tonkeeper.fragment.stake.confirm.rv.ConfirmStakeItemType
import com.tonapps.tonkeeper.fragment.stake.confirm.rv.ConfirmStakeListItem
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.wallet.data.account.entities.WalletEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class ConfirmStakeListHelper(
    private val mapper: ConfirmStakeListItemMapper
) {

    private val _items = MutableStateFlow(listOf<ConfirmStakeListItem>())
    val items: Flow<List<ConfirmStakeListItem>>
        get() = _items

    fun init(walletEntity: WalletEntity, pool: StakingPool) {
        _items.value = ConfirmStakeItemType.entries.map { mapper.map(it, walletEntity, pool) }
    }

    fun setFee(fee: Long) {
        val state = _items.value.toMutableList()
        val iterator = state.listIterator()
        while (iterator.hasNext()) {
            val current = iterator.next()
            if (current.itemType == ConfirmStakeItemType.FEE) {
                val updatedItem = current.copy(
                    textPrimary = TextWrapper.PlainString(fee.toString())
                )
                iterator.set(updatedItem)
            }
        }
        _items.value = state
    }
}