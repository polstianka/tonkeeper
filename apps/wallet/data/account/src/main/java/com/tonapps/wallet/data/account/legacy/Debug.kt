package com.tonapps.wallet.data.account.legacy

import com.tonapps.blockchain.ton.contract.WalletVersion
import com.tonapps.wallet.data.account.WalletSource
import com.tonapps.wallet.data.account.WalletType
import org.ton.api.pub.PublicKeyEd25519
import org.ton.crypto.hex

val IsDebug = false

val DebugWallet = WalletLegacy(
    id = 999,
    name = "Debug Wallet",
    publicKey = PublicKeyEd25519(hex("db642e022c80911fe61f19eb4f22d7fb95c1ea0b589c0f74ecf0cbf6db746c13")),
    version = WalletVersion.V4R2,
    type = WalletType.Default,
    emoji = "🔒",
    color = 0xFF000000.toInt(),
    source = WalletSource.Default
)