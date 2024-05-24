package com.tonapps.tonkeeper.ui.screen.settings.main

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import com.tonapps.tonkeeper.core.widget.balance.WidgetBalanceProvider
import com.tonapps.tonkeeper.popup.ActionSheet
import com.tonapps.tonkeeper.ui.screen.root.RootActivity
import com.tonapps.tonkeeper.ui.screen.settings.currency.CurrencyScreen
import com.tonapps.tonkeeper.ui.screen.settings.language.LanguageScreen
import com.tonapps.tonkeeper.ui.screen.name.edit.EditNameScreen
import com.tonapps.tonkeeper.ui.screen.notifications.NotificationsScreen
import com.tonapps.tonkeeper.ui.screen.settings.legal.LegalScreen
import com.tonapps.tonkeeper.ui.screen.settings.main.list.Adapter
import com.tonapps.tonkeeper.ui.screen.settings.main.list.Item
import com.tonapps.tonkeeper.ui.screen.settings.security.SecurityScreen
import com.tonapps.tonkeeper.ui.screen.settings.theme.ThemeScreen
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.wallet.data.core.SearchEngine
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.base.BaseListFragment
import uikit.dialog.alert.AlertDialog
import uikit.extensions.collectFlow
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.item.ItemTextView

class SettingsScreen: BaseListFragment(), BaseFragment.SwipeBack {

    private val settingsViewModel: SettingsViewModel by viewModel()

    private val searchEngineMenu: ActionSheet by lazy {
        ActionSheet(requireContext())
    }

    private val adapter = Adapter(::onClickItem)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle(getString(Localization.settings))
        setAdapter(adapter)

        collectFlow(settingsViewModel.uiItemsFlow, adapter::submitList)
    }

    private fun onClickItem(item: Item) {
        when (item) {
            is Item.Currency -> navigation?.add(CurrencyScreen.newInstance())
            is Item.Language -> navigation?.add(LanguageScreen.newInstance())
            is Item.Account -> navigation?.add(EditNameScreen.newInstance())
            is Item.Theme -> navigation?.add(ThemeScreen.newInstance())
            is Item.Widget -> installWidget()
            is Item.Security -> navigation?.add(SecurityScreen.newInstance())
            is Item.Legal -> navigation?.add(LegalScreen.newInstance())
            is Item.News -> navigation?.openURL(item.url, true)
            is Item.Support -> navigation?.openURL(item.url, true)
            is Item.Contact -> navigation?.openURL(item.url, true)
            is Item.Logout -> signOut()
            is Item.SearchEngine -> searchPicker(item)
            is Item.DeleteWatchAccount -> deleteWatchAccount()
            is Item.Rate -> openRate()
            is Item.Notifications -> navigation?.add(NotificationsScreen.newInstance())
            is Item.FAQ -> navigation?.openURL(item.url, true)
            else -> return
        }
    }

    private fun openRate() {
        val context = requireContext()
        val packageName = context.packageName.replace(".debug", "")
        val uri = "market://details?id=$packageName"
        val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
        if (intent.resolveActivity(context.packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun searchPicker(item: Item.SearchEngine) {
        if (searchEngineMenu.isShowing) {
            searchEngineMenu.dismiss()
            return
        }

        val index = adapter.currentList.indexOf(item)
        val itemView = findListItemView(index) as? ItemTextView ?: return

        searchEngineMenu.clearItems()
        for (searchEngine in SearchEngine.all) {
            val checkedIcon = if (searchEngine.title.equals(item.value, ignoreCase = true)) {
                getDrawable(UIKitIcon.ic_done_16)
            } else {
                null
            }
            searchEngineMenu.addItem(searchEngine.id, searchEngine.title, icon = checkedIcon)
        }
        searchEngineMenu.doOnItemClick = {
            settingsViewModel.setSearchEngine(SearchEngine.byId(it.id))
        }
        searchEngineMenu.show(itemView.dataView)
    }

    private fun installWidget() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val context = requireContext()
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val myProvider = ComponentName(context, WidgetBalanceProvider::class.java)
            if (!appWidgetManager.isRequestPinAppWidgetSupported) {
                return
            }
            val pinnedWidgetCallbackIntent = Intent(context, RootActivity::class.java)
            val successCallback = PendingIntent.getActivity(
                context, 0,
                pinnedWidgetCallbackIntent, PendingIntent.FLAG_IMMUTABLE
            )
            appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
        }
    }

    private fun signOut() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(Localization.sign_out_title)
        builder.setMessage(Localization.sign_out_description)
        builder.setNegativeButton(Localization.sign_out) {
            settingsViewModel.signOut()
            finish()
        }
        builder.setPositiveButton(Localization.cancel)
        builder.setColoredButtons()
        builder.show()
    }

    private fun deleteWatchAccount() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(Localization.delete_watch_account_alert)
        builder.setNegativeButton(Localization.delete) {
            settingsViewModel.signOut()
            finish()
        }
        builder.setPositiveButton(Localization.cancel)
        builder.setColoredButtons()
        builder.show()
    }

    companion object {
        fun newInstance() = SettingsScreen()
    }
}