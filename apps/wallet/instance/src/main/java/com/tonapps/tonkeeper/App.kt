package com.tonapps.tonkeeper

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.core.ImageTranscoderType
import com.facebook.imagepipeline.core.MemoryChunkType
import com.tonapps.tonkeeper.core.fiat.Fiat
import com.tonapps.tonkeeper.koin.koinModel
import com.tonapps.wallet.api.apiModule
import com.tonapps.wallet.data.account.accountModule
import com.tonapps.wallet.data.rates.ratesModule
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.token.tokenModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.tonapps.wallet.data.account.legacy.WalletManager
import com.tonapps.wallet.data.browser.browserModule
import com.tonapps.wallet.data.collectibles.collectiblesModule
import com.tonapps.wallet.data.core.dataModule
import com.tonapps.wallet.data.events.eventsModule
import com.tonapps.wallet.data.pools.poolsModule
import com.tonapps.wallet.data.push.pushModule
import com.tonapps.wallet.data.stonfi.stonfiModule
import com.tonapps.wallet.data.tonconnect.tonConnectModule
import org.koin.core.component.KoinComponent

class App: Application(), CameraXConfig.Provider, KoinComponent {

    companion object {

        @Deprecated("Use injection")
        lateinit var walletManager: WalletManager

        lateinit var fiat: Fiat

        @Deprecated("Use injection")
        lateinit var settings: SettingsRepository

        lateinit var db: AppDatabase
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        db = AppDatabase.getInstance(this)
        walletManager = WalletManager(this)
        fiat = Fiat(this)
        settings = SettingsRepository(this)

        startKoin {
            androidContext(this@App)
            modules(koinModel, dataModule, browserModule, pushModule, tonConnectModule, apiModule, accountModule, ratesModule, tokenModule, eventsModule, collectiblesModule, stonfiModule, poolsModule)
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        initFresco()
    }

    private fun initFresco() {
        val configBuilder = ImagePipelineConfig.newBuilder(this)
        configBuilder.setMemoryChunkType(MemoryChunkType.BUFFER_MEMORY)
        configBuilder.setImageTranscoderType(ImageTranscoderType.JAVA_TRANSCODER)
        configBuilder.experiment().setNativeCodeDisabled(true)
        configBuilder.setDownsampleEnabled(false)

        Fresco.initialize(this, configBuilder.build())
    }

    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder
            .fromConfig(Camera2Config.defaultConfig())
            .setMinimumLoggingLevel(Log.ERROR).build()
    }

    fun isOriginalAppInstalled(): Boolean {
        val pm = packageManager
        return try {
            pm.getPackageInfo("com.ton_keeper", 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}