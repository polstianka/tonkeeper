package com.tonapps.tonkeeper.ui.screen.card

import android.app.Application
import android.net.Uri
import com.tonapps.extensions.appVersionName
import com.tonapps.extensions.locale
import com.tonapps.tonkeeper.manager.tonconnect.TonConnectManager
import com.tonapps.tonkeeper.manager.tonconnect.bridge.JsonBuilder
import com.tonapps.tonkeeper.ui.base.InjectedTonConnectScreen
import com.tonapps.tonkeeper.ui.screen.card.entity.CardScreenPath
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import org.json.JSONObject

class CardViewModel(
    app: Application,
    private val wallet: WalletEntity,
    private val path: CardScreenPath,
    private val tonConnectManager: TonConnectManager,
    private val settingsRepository: SettingsRepository,
    private val api: API,
) : InjectedTonConnectScreen.ViewModel(app, wallet, tonConnectManager) {

    override val url: Uri by lazy {
        val builder = Uri.parse(api.getConfig(wallet.testnet).holdersAppEndpoint).buildUpon()

        val path = when (path) {
            is CardScreenPath.Account -> "/account/${path.accountId}"
            is CardScreenPath.Prepaid -> "/card-prepaid/${path.cardId}"
            is CardScreenPath.Create -> "/create"
            is CardScreenPath.Main -> "/accounts"
        }

        builder.appendEncodedPath(path)
        builder.appendQueryParameter("lang", context.locale.language)
        builder.appendQueryParameter("currency", settingsRepository.currency.code)
        builder.appendQueryParameter("theme", "tonkeeper")
        builder.appendQueryParameter(
            "theme-style", if (settingsRepository.theme.light) "light" else "dark"
        )
        builder.appendQueryParameter("utm_source", "tonkeeper")
        builder.build()
    }

    override suspend fun restoreConnection(currentUri: Uri?): JSONObject {
        return JsonBuilder.connectEventSuccess(wallet, null, null, context.appVersionName)
    }

}