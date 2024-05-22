package com.tonapps.tonkeeper.fragment.stake.pool_details

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.core.TextWrapper
import com.tonapps.tonkeeper.core.emit
import com.tonapps.tonkeeper.fragment.stake.domain.GetStakingPoolLiquidJettonCase
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingPool
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingService
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingSocial
import com.tonapps.tonkeeper.fragment.stake.domain.model.StakingSocialType
import com.tonapps.tonkeeper.fragment.stake.domain.model.tonViewerSocial
import com.tonapps.tonkeeper.fragment.stake.pool_details.presentation.LinksChipModel
import com.tonapps.tonkeeper.fragment.stake.presentation.formatApy
import com.tonapps.tonkeeper.fragment.stake.presentation.minStakingText
import com.tonapps.uikit.icon.UIKitIcon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.net.URI
import com.tonapps.wallet.localization.R as LocalizationR

class PoolDetailsViewModel(
    getStakingPoolLiquidJettonCase: GetStakingPoolLiquidJettonCase
) : ViewModel() {

    private val _events = MutableSharedFlow<PoolDetailsEvent>()
    private val args = MutableSharedFlow<PoolDetailsFragmentArgs>(replay = 1)

    val events: Flow<PoolDetailsEvent>
        get() = _events
    val title = args.map { it.pool.name }
    val pool = args.map { it.pool }
    val chips = args.map { it.service.chipModels(it.pool) }
    val liquidJetton = args.map { args ->
        getStakingPoolLiquidJettonCase.execute(
            args.pool,
            args.currency
        )
    }

    fun provideArgs(args: PoolDetailsFragmentArgs) {
        emit(this.args, args)
    }

    fun onCloseClicked() {
        emit(_events, PoolDetailsEvent.FinishFlow)
    }

    fun onChevronClicked() {
        emit(_events, PoolDetailsEvent.NavigateBack)
    }

    fun onChipClicked(model: LinksChipModel) {
        val event = PoolDetailsEvent.NavigateToLink(model.url)
        emit(_events, event)
    }

    fun onButtonClicked() = viewModelScope.launch {
        val pool = args.first().pool
        val event = PoolDetailsEvent.PickPool(pool)
        _events.emit(event)
    }
}

fun StakingService.chipModels(pool: StakingPool): List<LinksChipModel> {
    val completeSocials = socials + pool.tonViewerSocial()
    return completeSocials.map { it.toChipModel(this) }
}

fun StakingSocial.toChipModel(service: StakingService): LinksChipModel {
 return LinksChipModel(
        iconResId = type.getIconResId(),
        text = type.getText(service),
        url = link
    )
}

@DrawableRes
private fun StakingSocialType.getIconResId(): Int {
    return when (this) {
        StakingSocialType.LINK -> UIKitIcon.ic_globe_16
        StakingSocialType.TWITTER -> UIKitIcon.ic_twitter_16
        StakingSocialType.TELEGRAM -> UIKitIcon.ic_telegram_16
        StakingSocialType.TONVIEWER -> UIKitIcon.ic_magnifying_glass_16
    }
}

private fun StakingSocialType.getText(service: StakingService): TextWrapper {
    return when (this) {
        StakingSocialType.LINK -> {
            val social = service.socials.first { it.type == StakingSocialType.LINK }
            val uri = URI.create(social.link)
            TextWrapper.PlainString(uri.host)
        }

        StakingSocialType.TWITTER -> TextWrapper.StringResource(LocalizationR.string.twitter)
        StakingSocialType.TELEGRAM -> TextWrapper.StringResource(LocalizationR.string.community)
        StakingSocialType.TONVIEWER -> TextWrapper.StringResource(LocalizationR.string.tonviewer)
    }
}