package com.tonapps.tonkeeper.fragment.signer

import org.ton.api.pub.PublicKeyEd25519
import org.ton.cell.Cell
data class SignRequest(
    val cell: Cell,
    val publicKey: PublicKeyEd25519
)
