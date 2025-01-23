package com.tonapps.tonkeeper.manager.ledger.connection

import com.tonapps.ledger.ton.AccountPath
import com.tonapps.ledger.ton.LedgerAccount
import com.tonapps.ledger.ton.TonTransport
import com.tonapps.ledger.ton.Transaction
import com.tonapps.tonkeeper.ui.screen.ledger.steps.ConnectedDevice
import com.tonapps.tonkeeper.ui.screen.ledger.steps.ProofData
import com.tonapps.wallet.data.account.entities.WalletEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ton.cell.Cell

class LedgerBLEConnection(
    private val tonTransport: TonTransport,
    private val device: ConnectedDevice,
): LedgerConnection() {

    suspend fun getVersion(): String = withContext(Dispatchers.IO) {
        tonTransport.getVersion()
    }

    suspend fun getAccounts(): List<LedgerAccount> = withContext(Dispatchers.IO) {
        val accounts = mutableListOf<LedgerAccount>()
        for (i in 0 until 10) {
            accounts.add(getAccount(i))
        }
        accounts.toList()
    }

    suspend fun getAccount(index: Int): LedgerAccount = withContext(Dispatchers.IO) {
        tonTransport.getAccount(AccountPath(index))
    }

    suspend fun signTransaction(
        ledger: WalletEntity.Ledger,
        transaction: Transaction
    ): Cell = withContext(Dispatchers.IO) {
        val accountPath = AccountPath(ledger.accountIndex)
        tonTransport.signTransaction(accountPath, transaction)
    }

    suspend fun signAddressProof(
        ledger: WalletEntity.Ledger,
        proofData: ProofData
    ): ByteArray = withContext(Dispatchers.IO) {
        val accountPath = AccountPath(ledger.accountIndex)
        tonTransport.signAddressProof(
            path = accountPath,
            domain = proofData.domain,
            timestamp = proofData.timestamp,
            payload = proofData.payload
        )
    }

}