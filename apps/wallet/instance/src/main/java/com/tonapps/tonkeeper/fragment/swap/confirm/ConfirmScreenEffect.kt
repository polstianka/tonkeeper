package com.tonapps.tonkeeper.fragment.swap.confirm

sealed interface ConfirmScreenEffect {
    data object Success: ConfirmScreenEffect
    data object Fail: ConfirmScreenEffect
}