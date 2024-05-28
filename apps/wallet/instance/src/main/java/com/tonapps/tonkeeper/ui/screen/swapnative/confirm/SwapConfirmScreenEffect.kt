package com.tonapps.tonkeeper.ui.screen.swapnative.confirm

import org.ton.api.pub.PublicKeyEd25519
import org.ton.cell.Cell

sealed class SwapConfirmScreenEffect {
    data class CloseScreen(
        val navigateToHistory: Boolean
    ) : SwapConfirmScreenEffect()

    data class OpenSignerApp(
        val body: Cell,
        val publicKey: PublicKeyEd25519,
    ) : SwapConfirmScreenEffect()
}