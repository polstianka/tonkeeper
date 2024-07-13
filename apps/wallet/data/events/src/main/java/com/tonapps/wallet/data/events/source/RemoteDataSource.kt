package com.tonapps.wallet.data.events.source

import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.collectibles.CollectiblesRepository
import com.tonapps.wallet.data.events.entities.EventEntity
import io.tonapi.models.AccountEvent
import io.tonapi.models.AccountEvents

internal class RemoteDataSource(
    private val api: API
) {

    fun get(
        accountId: String,
        testnet: Boolean,
        beforeLt: Long? = null,
        limit: Int = 25
    ): AccountEvents = api.getEvents(accountId, testnet, beforeLt, limit)
}