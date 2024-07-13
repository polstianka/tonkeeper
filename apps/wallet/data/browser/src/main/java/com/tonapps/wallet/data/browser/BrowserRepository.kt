package com.tonapps.wallet.data.browser

import android.content.Context
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.browser.entities.BrowserAppEntity
import com.tonapps.wallet.data.browser.entities.BrowserDataEntity
import com.tonapps.wallet.data.browser.source.LocalDataSource
import com.tonapps.wallet.data.browser.source.RemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BrowserRepository(
    private val context: Context,
    private val api: API
) {

    private val localDataSource = LocalDataSource(context)
    private val remoteDataSource = RemoteDataSource(api)

    suspend fun search(
        country: String,
        query: String,
        testnet: Boolean,
    ): List<BrowserAppEntity> {
        val data = load(country, testnet) ?: return emptyList()
        val all = data.categories.map { it.apps }.flatten()
        return all.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true) ||
            it.url.toString().contains(query, ignoreCase = true)
        }.distinctBy { it.url }
    }

    suspend fun load(country: String, testnet: Boolean): BrowserDataEntity? = withContext(Dispatchers.IO) {
        loadLocal(country) ?: loadRemote(country, testnet)
    }

    private fun loadLocal(country: String): BrowserDataEntity? {
        return localDataSource.getCache(country)
    }

    suspend fun loadRemote(country: String, testnet: Boolean): BrowserDataEntity? {
        val data = remoteDataSource.load(testnet) ?: return null
        localDataSource.setCache(country, data)
        return data
    }
}