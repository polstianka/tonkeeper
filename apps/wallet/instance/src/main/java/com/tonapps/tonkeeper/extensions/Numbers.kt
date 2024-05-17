package com.tonapps.tonkeeper.extensions

val Float.amount :String
    get() {
        return if (0f >= this) {
            ""
        } else {
            this.toString()
        }
    }