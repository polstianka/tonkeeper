package com.tonapps.tonkeeper

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.core.ImageTranscoderType
import com.facebook.imagepipeline.core.MemoryChunkType
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.koin.koinModel
import com.tonapps.wallet.api.apiModule
import com.tonapps.wallet.data.account.accountModule
import com.tonapps.wallet.data.rates.ratesModule
import com.tonapps.wallet.data.token.tokenModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.tonapps.wallet.data.backup.backupModule
import com.tonapps.wallet.data.battery.batteryModule
import com.tonapps.wallet.data.browser.browserModule
import com.tonapps.wallet.data.collectibles.collectiblesModule
import com.tonapps.wallet.data.core.Theme
import com.tonapps.wallet.data.core.dataModule
import com.tonapps.wallet.data.events.eventsModule
import com.tonapps.wallet.data.passcode.passcodeModule
import com.tonapps.wallet.data.purchase.purchaseModule
import com.tonapps.wallet.data.push.pushModule
import com.tonapps.wallet.data.rn.rnLegacyModule
import com.tonapps.wallet.data.staking.stakingModule
import com.tonapps.wallet.data.tonconnect.tonConnectModule
import org.koin.core.component.KoinComponent

class App: Application(), CameraXConfig.Provider, KoinComponent {

    companion object {

        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        Theme.add("blue", uikit.R.style.Theme_App_Blue)
        Theme.add("dark", uikit.R.style.Theme_App_Dark)
        Theme.add("light", uikit.R.style.Theme_App_Light, true)

        instance = this

        startKoin {
            androidContext(this@App)
            modules(koinModel, purchaseModule, batteryModule, stakingModule, passcodeModule, rnLegacyModule, backupModule, dataModule, browserModule, pushModule, tonConnectModule, apiModule, accountModule, ratesModule, tokenModule, eventsModule, collectiblesModule)
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        initFresco()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        CurrencyFormatter.onConfigurationChanged(newConfig)
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