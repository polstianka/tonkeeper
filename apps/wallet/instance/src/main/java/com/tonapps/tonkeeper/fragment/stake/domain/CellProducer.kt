package com.tonapps.tonkeeper.fragment.stake.domain

import org.ton.cell.Cell

interface CellProducer {
    fun produce(): Cell
}