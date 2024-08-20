package com.tonapps.tonkeeper.core.history

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.tonapps.wallet.localization.Localization
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.wallet.data.account.entities.WalletEntity
import io.tonapi.models.AccountAddress
import io.tonapi.models.Action

@get:DrawableRes
val ActionType.iconRes: Int
    get() = when (this) {
        ActionType.Received, ActionType.NftReceived, ActionType.JettonMint -> R.drawable.ic_tray_arrow_down_28
        ActionType.Send, ActionType.NftSend, ActionType.AuctionBid -> UIKitIcon.ic_tray_arrow_up_28
        ActionType.CallContract, ActionType.DepositStake, ActionType.Unknown -> UIKitIcon.ic_gear_28
        ActionType.Swap -> R.drawable.ic_swap_horizontal_alternative_28
        ActionType.DeployContract, ActionType.WithdrawStakeRequest, ActionType.WithdrawStake -> UIKitIcon.ic_donemark_28
        ActionType.DomainRenewal -> R.drawable.ic_return_28
        ActionType.NftPurchase -> R.drawable.ic_shopping_bag_28
        ActionType.JettonBurn -> R.drawable.ic_fire_28
        ActionType.UnSubscribe -> R.drawable.ic_xmark_28
        ActionType.Subscribe -> R.drawable.ic_bell_28
        ActionType.Fee -> R.drawable.ic_ton_28
    }


@get:StringRes
val ActionType.nameRes: Int
    get() = when (this) {
        ActionType.Received, ActionType.NftReceived, ActionType.JettonMint -> Localization.received
        ActionType.Send, ActionType.NftSend -> Localization.sent
        ActionType.CallContract -> Localization.call_contract
        ActionType.Swap -> Localization.swap
        ActionType.DeployContract -> Localization.wallet_initialized
        ActionType.DepositStake -> Localization.stake
        ActionType.AuctionBid -> Localization.bid
        ActionType.WithdrawStakeRequest -> Localization.unstake_request
        ActionType.DomainRenewal -> Localization.domain_renew
        ActionType.WithdrawStake -> Localization.unstake
        ActionType.Unknown -> Localization.unknown
        ActionType.NftPurchase -> Localization.nft_purchase
        ActionType.JettonBurn -> Localization.burned
        ActionType.UnSubscribe -> Localization.unsubscribed
        ActionType.Subscribe -> Localization.subscribed
        ActionType.Fee -> Localization.network_fee
    }

val Action.recipient: AccountAddress?
    get() = tonTransfer?.recipient ?: jettonTransfer?.recipient ?: nftItemTransfer?.recipient
