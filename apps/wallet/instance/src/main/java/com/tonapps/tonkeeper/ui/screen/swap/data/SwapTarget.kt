package com.tonapps.tonkeeper.ui.screen.swap.data

enum class SwapTarget {
    RECEIVE {
        override val reverse: SwapTarget
            get() = SEND
    },

    SEND {
        override val reverse: SwapTarget
            get() = RECEIVE
    };

    abstract val reverse: SwapTarget
}