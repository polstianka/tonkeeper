package com.tonapps.tonkeeper.ui.adapter

import android.net.Uri
import androidx.annotation.StringRes
import com.tonapps.tonkeeper.api.chart.ChartEntity
import com.tonapps.tonkeeper.api.chart.ChartPeriod
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.entity.BalanceStakeEntity
import com.tonapps.wallet.data.account.WalletType
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.localization.Localization
import java.math.BigDecimal
import java.math.BigInteger

sealed class Item(
    type: Int
) : com.tonapps.uikit.list.BaseListItem(type) {
    enum class Type {
        LINKS,
        TOKEN,
        TOKEN_SUGGESTED,
        TOKEN_SUGGESTIONS,
        FIAT_METHOD,

        TITLE_H3,
        TITLE_LABEL1,
        DESCRIPTION_BODY3,
        CHART_LEGEND,

        STAKING_PAGE_HEADER,
        STAKING_PAGE_ACTIONS,
        STAKING_PAGE_CHART,
        STAKING_PAGE_CHART_PERIOD,
        STAKING_PAGE_CHART_HEADER,
        STAKING_PAGE_POOL_INFO_ROWS,
        STAKING_PAGE_PENDING_ACTION,
        STAKING_PAGE_POOL_IMPLEMENTATION,
        STAKING_PAGE_POOL,

        OFFSET_8DP,
        OFFSET_16DP,
        OFFSET_32DP,
        ;

        companion object {
            private const val START = 1.shl(24)
        }

        val value: Int
            get() = START + this.ordinal
    }


    data class TitleH3(
        @StringRes val res: Int,
        val text: String?
    ) : Item(Type.TITLE_H3.value) {
        constructor(@StringRes res: Int): this(res = res, text = null)
        constructor(text: String): this(res = 0, text = text)
    }

    data class TitleLabel1(
        @StringRes val res: Int,
        val text: String?
    ) : Item(Type.TITLE_LABEL1.value) {
        constructor(@StringRes res: Int): this(res = res, text = null)
        constructor(text: String): this(res = 0, text = text)
    }

    data class DescriptionBody3(
        @StringRes val res: Int,
        val text: String?
    ) : Item(Type.DESCRIPTION_BODY3.value) {
        constructor(@StringRes res: Int): this(res = res, text = null)
        constructor(text: String): this(res = 0, text = text)
    }

    data class ChartLegend(
        val text: String
    ) : Item(Type.CHART_LEGEND.value)

    enum class TokenDisplayMode {
        Default, SwapSelector
    }

    data class Token(
        val position: ListCell.Position,
        val iconUri: Uri,
        val address: String,
        val symbol: String,
        val name: String,
        val balance: Float,
        val balanceFormat: CharSequence,
        val fiat: Float,
        val fiatFormat: CharSequence,
        val rate: CharSequence,
        val rateDiff24h: String,
        val verified: Boolean,
        val testnet: Boolean,
        val hiddenBalance: Boolean,

        val mode: TokenDisplayMode,
        val onClickListener: (() -> Unit)?
    ) : Item(Type.TOKEN.value)



    enum class StakingPoolActionType {
        PendingDeposit, PendingWithdraw, ReadyWithdraw;

        val title: Int get() = when(this) {
            PendingDeposit -> Localization.staking_pending_deposit_title
            PendingWithdraw -> Localization.staking_pending_withdrawal_title
            ReadyWithdraw -> Localization.staking_ready_withdrawal_title
        }
    }

    data class StakingPagePendingAction(
        val action: StakingPoolActionType,
        val position: ListCell.Position,
        val stake: BalanceStakeEntity,
        val balance: Float,
        val balanceFormat: CharSequence,
        val fiat: Float,
        val fiatFormat: CharSequence,
        val cycleEnd: Long,
        val testnet: Boolean,
    ) : Item(Type.STAKING_PAGE_PENDING_ACTION.value)


    data class TokenSuggestions(
        val list: List<TokenSuggested>
    ): Item(Type.TOKEN_SUGGESTIONS.value)

    data class TokenSuggested(
        val iconUri: Uri,
        val address: String,
        val symbol: String,
        val name: String,
        val onClickListener: (() -> Unit)?
    ) : Item(Type.TOKEN_SUGGESTED.value)

    data class Links(
        val links: List<Uri>
    ) : Item(Type.LINKS.value)

    data class FiatMethod(
        val position: ListCell.Position,
        val id: String,
        val title: String,
        val subtitle: String,
        val iconUri: Uri,
        val checked: Boolean,
        val onClickListener: (() -> Unit)?
    ) : Item(Type.FIAT_METHOD.value)

    data class StakingPageHeader(
        val balance: CharSequence,
        val currencyBalance: CharSequence,
        val iconUri: Uri,
    ) : Item(Type.STAKING_PAGE_HEADER.value)

    data class StakingPageActions(
        val walletType: WalletType,
        val poolAddress: String,
        val stake: AccountTokenEntity?
    ) : Item(Type.STAKING_PAGE_ACTIONS.value)

    data class StakingPageChart(
        val period: ChartPeriod,
        val data: List<ChartEntity>
    ) : Item(Type.STAKING_PAGE_CHART.value)

    data class StakingPageChartPeriod(
        val listener: (period: ChartPeriod) -> Unit,
        val period: ChartPeriod
    ) : Item(Type.STAKING_PAGE_CHART_PERIOD.value)

    data class StakingPageChartHeader(
        val apy: String
    ) : Item(Type.STAKING_PAGE_CHART_HEADER.value)

    data class StakingPagePoolInfoRows(
        val minimalDeposit: String,
        val apy: String,
        val isMaxApy: Boolean
    ) : Item(Type.STAKING_PAGE_POOL_INFO_ROWS.value)

    data class PoolImplementation(
        val position: ListCell.Position,
        val name: String,
        val iconRes: Int,
        val minStakeNano: BigInteger,
        val maxApy: BigDecimal,
        val poolsCount: Int,
        val isMaxApy: Boolean,
        val isChecked: Boolean,
        val onClickListener: (() -> Unit)?
    ) : Item(Type.STAKING_PAGE_POOL_IMPLEMENTATION.value)

    data class Pool(
        val position: ListCell.Position,
        val name: String,
        val iconRes: Int,
        val apy: BigDecimal,
        val isMaxApy: Boolean,
        val isChecked: Boolean,
        val onClickListener: (() -> Unit)?
    ) : Item(Type.STAKING_PAGE_POOL.value)

    data object Offset32 : Item(Type.OFFSET_32DP.value)
    data object Offset16 : Item(Type.OFFSET_16DP.value)
    data object Offset8 : Item(Type.OFFSET_8DP.value)
}