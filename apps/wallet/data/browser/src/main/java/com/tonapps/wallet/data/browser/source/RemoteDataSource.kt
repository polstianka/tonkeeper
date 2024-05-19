package com.tonapps.wallet.data.browser.source

import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.browser.entities.BrowserDataEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class RemoteDataSource(
    private val api: API
) {

    suspend fun load(testnet: Boolean): BrowserDataEntity? = withContext(Dispatchers.IO) {
        try {
            BrowserDataEntity(api.getBrowserApps(testnet))
        } catch (e: Throwable) {
            null
        }
    }
}