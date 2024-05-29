package com.tonapps.wallet.data.token

import com.tonapps.wallet.api.entity.BalanceEntity
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.rates.entity.RatesEntity
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.data.token.entities.TokenRateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger

@Deprecated("USE V2")
class TokenRepository(
    private val ratesRepository: RatesRepository,
    private val tokensRepository: RawTokensRepository,
) {
    suspend fun get(
        currency: WalletCurrency,
        accountId: String,
        testnet: Boolean
    ): List<AccountTokenEntity> {
        val tokens = getLocal(currency, accountId, testnet)
        if (tokens.isNotEmpty()) {
            return tokens
        }
        return getRemote(currency, accountId, testnet)
    }

    suspend fun getRemote(
        currency: WalletCurrency,
        accountId: String,
        testnet: Boolean
    ): List<AccountTokenEntity> = withContext(Dispatchers.IO) {
        val balances = load(currency, accountId, testnet)
        if (testnet) {
            return@withContext buildTokens(currency, balances, RatesEntity.empty(currency), true)
        }
        val rates = ratesRepository.getRates(currency, balances.map {
            it.token.address
        })
        buildTokens(currency, balances, rates, false)
    }

    suspend fun getLocal(
        currency: WalletCurrency,
        accountId: String,
        testnet: Boolean
    ): List<AccountTokenEntity> = withContext(Dispatchers.IO) {
        val balances = cache(accountId, testnet)
        if (testnet) {
            return@withContext buildTokens(currency, balances, RatesEntity.empty(currency), true)
        }

        val rates = ratesRepository.cache(currency, balances.map {
            it.token.address
        })
        if (rates.isEmpty) {
            emptyList()
        } else {
            buildTokens(currency, balances, rates, false)
        }
    }

    private fun buildTokens(
        currency: WalletCurrency,
        balances: List<BalanceEntity>,
        rates: RatesEntity,
        testnet: Boolean
    ): List<AccountTokenEntity> {
        val verified = mutableListOf<AccountTokenEntity>()
        val unverified = mutableListOf<AccountTokenEntity>()
        for (balance in balances) {
            val tokenAddress = balance.token.address
            if (balance.nano == BigInteger.ZERO && !balance.token.isTon) {
                continue
            }
            val tokenRate = TokenRateEntity(
                currency = currency,
                fiat = rates.convert(tokenAddress, balance.value),
                rate = rates.getRate(tokenAddress),
                rateDiff24h = rates.getDiff24h(tokenAddress)
            )
            val token = AccountTokenEntity(
                balance = balance,
                rate = tokenRate
            )
            if (token.verified) {
                verified.add(token)
            } else {
                unverified.add(token)
            }
        }
        if (testnet) {
            return sortTestnet(verified + unverified)
        }
        return sort(verified) + sort(unverified)
    }

    private fun sort(list: List<AccountTokenEntity>): List<AccountTokenEntity> {
        return list.sortedWith { first, second ->
            when {
                first.isTon -> -1
                second.isTon -> 1
                else -> second.fiat.compareTo(first.fiat)
            }
        }
    }

    private fun sortTestnet(list: List<AccountTokenEntity>): List<AccountTokenEntity> {
        return list.sortedWith { first, second ->
            when {
                first.isTon -> -1
                second.isTon -> 1
                else -> second.balance.value.compareTo(first.balance.value)
            }
        }
    }

    private fun cache(
        accountId: String,
        testnet: Boolean
    ): List<BalanceEntity> {
        return tokensRepository.getLocal(accountId, testnet) ?: emptyList()
    }

    private suspend fun load(
        currency: WalletCurrency,
        accountId: String,
        testnet: Boolean
    ): List<BalanceEntity> = withContext(Dispatchers.IO) {
        tokensRepository.getRemote(currency, accountId, testnet)
    }
}