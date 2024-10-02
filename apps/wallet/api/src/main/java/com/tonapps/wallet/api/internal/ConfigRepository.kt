package com.tonapps.wallet.api.internal

import android.content.Context
import android.util.Log
import com.tonapps.extensions.file
import com.tonapps.extensions.state
import com.tonapps.extensions.toByteArray
import com.tonapps.extensions.toParcel
import com.tonapps.wallet.api.entity.ConfigEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ConfigRepository(
    context: Context,
    scope: CoroutineScope,
    private val internalApi: InternalApi,
) {

    private val configFile = context.cacheDir.file("config")
    private val _stream = MutableStateFlow<ConfigEntity?>(null)

    val stream = _stream.state(scope).filterNotNull()

    var configEntity: ConfigEntity? = null
        private set (value) {
            field = value
            _stream.value = value
        }

    init {
        readCache()?.let {
            configEntity = it
        }
        scope.launch(Dispatchers.Main) {
            remote(false)?.let {
                configEntity = it
            }
        }
    }

    private fun readCache(): ConfigEntity? {
        if (configFile.exists() && configFile.length() > 0) {
            return configFile.readBytes().toParcel()
        }
        return null
    }

    private suspend fun remote(testnet: Boolean): ConfigEntity? = withContext(Dispatchers.IO) {
        val config = internalApi.downloadConfig(testnet) ?: return@withContext null
        configFile.writeBytes(config.toByteArray())
        config
    }

}