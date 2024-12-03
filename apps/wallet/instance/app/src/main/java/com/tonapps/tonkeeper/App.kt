package com.tonapps.tonkeeper

import android.app.Application
import android.content.res.Configuration
import android.os.Build
import android.os.StrictMode
import android.util.Log
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.DownsampleMode
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.core.ImageTranscoderType
import com.facebook.imagepipeline.core.MemoryChunkType
import com.tonapps.extensions.setLocales
import com.tonapps.icu.CurrencyFormatter
import com.tonapps.tonkeeper.koin.koinModel
import com.tonapps.tonkeeper.koin.viewModelWalletModule
import com.tonapps.tonkeeper.koin.workerModule
import com.tonapps.tonkeeperx.BuildConfig
import com.tonapps.wallet.api.apiModule
import com.tonapps.wallet.data.account.accountModule
import com.tonapps.wallet.data.rates.ratesModule
import com.tonapps.wallet.data.token.tokenModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.tonapps.wallet.data.backup.backupModule
import com.tonapps.wallet.data.battery.batteryModule
import com.tonapps.wallet.data.browser.browserModule
import com.tonapps.wallet.data.cards.cardsModule
import com.tonapps.wallet.data.collectibles.collectiblesModule
import com.tonapps.wallet.data.contacts.contactsModule
import com.tonapps.wallet.data.core.Theme
import com.tonapps.wallet.data.core.dataModule
import com.tonapps.wallet.data.dapps.dAppsModule
import com.tonapps.wallet.data.events.eventsModule
import com.tonapps.wallet.data.passcode.passcodeModule
import com.tonapps.wallet.data.purchase.purchaseModule
import com.tonapps.wallet.data.rn.rnLegacyModule
import com.tonapps.wallet.data.settings.SettingsRepository
import com.tonapps.wallet.data.staking.stakingModule
import org.koin.core.component.KoinComponent
import org.koin.android.ext.android.inject
import org.koin.androidx.workmanager.koin.workManagerFactory
import java.util.concurrent.Executors

class App: Application(), CameraXConfig.Provider, KoinComponent {

    companion object {

        lateinit var instance: App

        fun applyConfiguration(newConfig: Configuration) {
            CurrencyFormatter.onConfigurationChanged(newConfig)
        }
    }

    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate() {
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .penaltyListener(Executors.newSingleThreadExecutor()) {
                    Log.e("TonkeeperStrictModeLog", "StrictMode.VmPolicy: $it", it.cause)
                }.build())
        }

        super.onCreate()
        Theme.add("system", 0)
        Theme.add("blue", uikit.R.style.Theme_App_Blue)
        Theme.add("dark", uikit.R.style.Theme_App_Dark)
        Theme.add("light", uikit.R.style.Theme_App_Light, true)

        instance = this

        startKoin {
            androidContext(this@App)
            modules(koinModel, contactsModule, workerModule, dAppsModule, viewModelWalletModule, purchaseModule, batteryModule, stakingModule, passcodeModule, rnLegacyModule, backupModule, dataModule, browserModule, apiModule, accountModule, ratesModule, tokenModule, eventsModule, collectiblesModule, cardsModule)
            workManagerFactory()
        }
        setLocales(settingsRepository.localeList)
        initFresco()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyConfiguration(newConfig)
    }

    private fun initFresco() {
        val configBuilder = ImagePipelineConfig.newBuilder(this)
        configBuilder.setMemoryChunkType(MemoryChunkType.BUFFER_MEMORY)
        configBuilder.setImageTranscoderType(ImageTranscoderType.JAVA_TRANSCODER)
        configBuilder.experiment().setNativeCodeDisabled(true)
        configBuilder.experiment().setPartialImageCachingEnabled(false)
        configBuilder.experiment().setUseDownsampligRatioForResizing(true)
        configBuilder.experiment().useBitmapPrepareToDraw = true
        configBuilder.setDownsampleMode(DownsampleMode.ALWAYS)

        Fresco.initialize(this, configBuilder.build())
    }

    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder
            .fromConfig(Camera2Config.defaultConfig())
            .setMinimumLoggingLevel(Log.ERROR).build()
    }
}