package com.tonapps.tonkeeper.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.Person
import androidx.core.content.LocusIdCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.graphics.drawable.IconCompat
import com.tonapps.emoji.Emoji
import com.tonapps.tonkeeper.App
import com.tonapps.wallet.data.account.entities.WalletEntity

object ShortcutHelper {

    fun shortcutAction(
        context: Context,
        titleRes: Int,
        iconRes: Int,
        url: String
    ): ShortcutInfoCompat {
        val icon = IconCompat.createWithResource(context, iconRes)

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setPackage(context.packageName)

        val builder = ShortcutInfoCompat.Builder(context, url)
            .setShortLabel(context.getString(titleRes))
            .setIcon(icon)
            .setIntent(intent)

        return builder.build()
    }

    suspend fun shortcutWallet(
        context: Context,
        wallet: WalletEntity
    ): ShortcutInfoCompat {
        val url = "tonkeeper://pick/${wallet.id}"
        val emojiBitmap = Emoji.getBitmap(context, wallet.label.emoji)
        val icon = IconCompat.createWithBitmap(emojiBitmap)

        val person = Person.Builder()
            .setName(wallet.label.name)
            .setIcon(icon)
            .setUri(url)
            .setKey(wallet.id.toString())
            .build()

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setPackage(context.packageName)

        val builder = ShortcutInfoCompat.Builder(context, "wallet_${wallet.id}")
            .setShortLabel(wallet.label.name)
            .setIcon(icon)
            .setPerson(person)
            .setLongLived(true)
            .setLocusId(LocusIdCompat(wallet.id.toString()))
            .setIntent(intent)

        return builder.build()
    }
}