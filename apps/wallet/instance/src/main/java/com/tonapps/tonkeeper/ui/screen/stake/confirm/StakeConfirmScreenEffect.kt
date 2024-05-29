package com.tonapps.tonkeeper.ui.screen.stake.confirm

import org.ton.api.pub.PublicKeyEd25519
import org.ton.cell.Cell
import uikit.mvi.UiEffect

sealed class StakeConfirmScreenEffect : UiEffect() {
    data class CloseScreenStake(
        val navigateToHistory: Boolean
    ) : StakeConfirmScreenEffect()

    data class OpenSignerApp(
        val body: Cell,
        val publicKey: PublicKeyEd25519,
    ) : StakeConfirmScreenEffect()
}