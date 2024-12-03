package com.tonapps.wallet.api.internal

import android.content.Context
import android.util.Log
import com.tonapps.extensions.file
import com.tonapps.extensions.toByteArray
import com.tonapps.extensions.toParcel
import com.tonapps.wallet.api.entity.ConfigEntity
import com.tonapps.wallet.api.entity.ConfigResponseEntity
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

    private val configFile = context.cacheDir.file("config_all")
    private val _stream = MutableStateFlow(ConfigEntity.default)

    val stream = _stream.asStateFlow()

    var configEntity: ConfigEntity = ConfigEntity.default
        private set (value) {
            field = value
            _stream.value = value.copy()
        }

    var configTestnetEntity: ConfigEntity = ConfigEntity.default
        private set

    init {
        scope.launch(Dispatchers.IO) {
            readCache()?.let {
                setConfig(it)
            }
            remote()?.let {
                setConfig(it)
            }
        }
    }

    private suspend fun setConfig(config: ConfigResponseEntity) = withContext(Dispatchers.Main) {
        configEntity = config.mainnet
        configTestnetEntity = config.testnet
    }

    private fun readCache(): ConfigResponseEntity? {
        if (configFile.exists() && configFile.length() > 0) {
            return configFile.readBytes().toParcel()
        }
        return null
    }

    private suspend fun remote(): ConfigResponseEntity? = withContext(Dispatchers.IO) {
        val response = internalApi.downloadConfig() ?: return@withContext null
        configFile.writeBytes(response.toByteArray())
        response
    }

}