package com.tonapps.tonkeeper.client.safemode

import android.content.Context
import android.net.Uri
import android.util.Log
import com.tonapps.extensions.MutableEffectFlow
import com.tonapps.icu.Coins
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.core.BlobDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class SafeModeClient(
    private val context: Context,
    private val api: API,
    private val scope: CoroutineScope
) {

    private val scamDomains = ConcurrentHashMap<String, Boolean>()
    private val blobCache = BlobDataSource.simple<BadDomainsEntity>(context, "safemode")
    private val badDomainsFlow = flow {
        getCachedBadDomains()?.let {
            emit(it)
        }

        loadBadDomains()?.let {
            emit(it)
        }
    }.distinctUntilChanged()

    private val _isReadyFlow = MutableStateFlow<Boolean?>(null)
    val isReadyFlow = _isReadyFlow.asStateFlow().filterNotNull()

    init {
        badDomainsFlow.onEach {
            it.array.forEach { domain ->
                if (domain.isNotBlank()) {
                    scamDomains[domain] = true
                }
            }
            _isReadyFlow.value = true
        }.launchIn(scope)
    }

    fun isHasScamUris(vararg uris: Uri): Boolean {
        for (uri in uris) {
            if (uri == Uri.EMPTY || uri.scheme != "https") {
                continue
            }
            var host = uri.host ?: continue
            if (host.startsWith("www.")) {
                host = host.substring(4)
            }
            if (scamDomains.containsKey(host)) {
                return true
            }
        }
        return false
    }

    private fun getCachedBadDomains(): BadDomainsEntity? {
        val entity = blobCache.getCache("scam_domains")
        if (entity == null || entity.isEmpty) {
            return null
        }
        return entity
    }

    private suspend fun loadBadDomains(): BadDomainsEntity? {
        val domains = api.getScamDomains()
        if (domains.isEmpty()) {
            return null
        }
        val entity = BadDomainsEntity(domains)
        blobCache.setCache("scam_domains", entity)
        return entity
    }

}