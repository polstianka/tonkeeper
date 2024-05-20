package com.tonapps.tonkeeper.fragment.jetton.list

import com.tonapps.wallet.data.account.WalletType
import io.tonapi.models.JettonBalance

sealed class JettonItem(
    type: Int
) : com.tonapps.uikit.list.BaseListItem(type) {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ACTIONS = 1
        const val TYPE_DIVIDER = 2
        const val TYPE_ACTIONS_STAKED = 3
        const val TYPE_DESCRIPTION = 4
        const val TYPE_DETAILS = 5
        const val TYPE_LINKS = 6
    }

    data class Header(
        val balance: CharSequence,
        val currencyBalance: CharSequence,
        val iconUrl: String,
        val rate: CharSequence?,
        val diff24h: String?,
        val staked: Boolean = false
    ) : JettonItem(TYPE_HEADER)

    data class Actions(
        val wallet: String,
        val jetton: JettonBalance,
        val walletType: WalletType
    ) : JettonItem(TYPE_ACTIONS)

    data class ActionsStaked(
        val wallet: String,
        val jetton: JettonBalance,
        val walletType: WalletType
    ) : JettonItem(TYPE_ACTIONS_STAKED)

    data class Description(
        val text: String
    ) : JettonItem(TYPE_DESCRIPTION)

    data class Details(
        val isApyMax: Boolean,
        val apy: String,
        val minDeposit: String,
    ) : JettonItem(TYPE_DETAILS)

    data class Links(
        val links: List<String>
    ) : JettonItem(TYPE_LINKS)

    data object Divider : JettonItem(TYPE_DIVIDER)
}