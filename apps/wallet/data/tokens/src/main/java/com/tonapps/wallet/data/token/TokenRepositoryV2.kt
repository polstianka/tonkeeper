package com.tonapps.wallet.data.token

import com.tonapps.wallet.api.entity.BalanceEntity
import com.tonapps.wallet.api.entity.BalanceStakeEntity
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.api.entity.pool.PoolStakeEntity
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.pools.StakingPoolsRepository
import com.tonapps.wallet.data.pools.entities.StakingPoolsEntity
import com.tonapps.wallet.data.rates.RatesRepository
import com.tonapps.wallet.data.rates.entity.RatesEntity
import com.tonapps.wallet.data.token.entities.AccountTokenEntity
import com.tonapps.wallet.data.token.entities.TokenRateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.math.BigInteger

class TokenRepositoryV2(
    private val ratesRepository: RatesRepository,
    private val poolsRepository: StakingPoolsRepository,
    private val stakesRepository: RawStakesRepository,
    private val tokensRepository: RawTokensRepository,
) {

    data class Storage(
        val rates: RatesRepository.Storage,
        val pools: StakingPoolsRepository.Storage,
        val stakes: RawStakesRepository.Storage,
        val tokens: RawTokensRepository.Storage
    ) {
        private val totalBalanceCache = HashMap<String, Float>()
        private val assetsCache = HashMap<String, List<AccountTokenEntity>>()

        fun totalBalance(accountId: String, testnet: Boolean, currency: WalletCurrency): Float {
            val key = RawTokensRepository.Request.requestKey(accountId, testnet, currency)
            return totalBalanceCache[key] ?: buildTotalBalance(accountId, testnet, currency)
        }

        private fun buildTotalBalance(accountId: String, testnet: Boolean, currency: WalletCurrency): Float {
            val key = RawTokensRepository.Request.requestKey(accountId, testnet, currency)

            val assets = assets(accountId, testnet, currency)
            var fiatBalance = 0f
            if (testnet) {
                fiatBalance = assets.first().balance.value
            } else {
                for (token in assets) {
                    fiatBalance += token.fiat
                }
            }

            totalBalanceCache[key] = fiatBalance
            return fiatBalance
        }

        fun assets(accountId: String, testnet: Boolean, currency: WalletCurrency): List<AccountTokenEntity> {
            val key = RawTokensRepository.Request.requestKey(accountId, testnet, currency)
            return assetsCache[key] ?: buildAssets(accountId, testnet, currency)
        }

        private fun buildAssets(accountId: String, testnet: Boolean, currency: WalletCurrency): List<AccountTokenEntity> {
            val key = RawTokensRepository.Request.requestKey(accountId, testnet, currency)

            val balances = tokens.get(accountId, testnet, currency)?.result?.list ?: return emptyList()
            val pools = pools.get(accountId, testnet)?.result
            val stakes = stakes.get(accountId, testnet)?.result?.list ?: emptyList()
            val ratesCurr = rates.get(currency)?.result
            val ratesTon = rates.get(WalletCurrency.TON)?.result

            val result = buildTokens(
                accountId = accountId,
                currency = currency,
                testnet = testnet,
                balances = balances,
                stakes = stakes,
                rates = ratesCurr,
                poolsOpt = pools,
                ratesTon = ratesTon
            )

            assetsCache[key] = result
            return result
        }
    }

    val storageFlow = combine(ratesRepository.storageFlow, poolsRepository.storageFlow, stakesRepository.storageFlow, tokensRepository.storageFlow) {
        rates, pools, stakes, tokens -> Storage(rates, pools, stakes, tokens)
    }

    val storageFlowIgnoreRates = combine(poolsRepository.storageFlow, stakesRepository.storageFlow, tokensRepository.storageFlow) {
        pools, stakes, tokens -> Storage(ratesRepository.storage(), pools, stakes, tokens)
    }





    suspend fun doRequest(
        accountId: String,
        testnet: Boolean,
        currency: WalletCurrency
    ): Boolean = withContext(Dispatchers.IO) {
        val a = async { poolsRepository.doRequest(accountId, testnet) }
        val b = async { tokensRepository.doRequest(accountId, testnet, currency) }
        val c = async { stakesRepository.doRequest(accountId, testnet) }

        a.await() != null && b.await() != null && c.await() != null
    }

    suspend fun doRequestWithoutAllPools(
        accountId: String,
        testnet: Boolean,
        currency: WalletCurrency
    ): Boolean = withContext(Dispatchers.IO) {
        val b = async { tokensRepository.doRequest(accountId, testnet, currency) }
        val c = async { stakesRepository.doRequest(accountId, testnet) }

        b.await() != null && c.await() != null
    }

    companion object {
        private fun buildTokens(
            accountId: String,
            currency: WalletCurrency,
            balances: List<BalanceEntity>,
            poolsOpt: StakingPoolsEntity?,
            stakes: List<PoolStakeEntity>,
            rates: RatesEntity?,
            ratesTon: RatesEntity?,
            testnet: Boolean
        ): List<AccountTokenEntity> {
            val verified = mutableListOf<AccountTokenEntity>()
            val unverified = mutableListOf<AccountTokenEntity>()

            val processedTokens = mutableSetOf<String>()

            poolsOpt?.let { pools ->
                for (stake in stakes) {
                    pools.pools[stake.pool]?.let { pool ->
                        if (pool.liquidJettonMaster == null) {
                            val token = TokenEntity.TON
                            val stakeEntity = BalanceStakeEntity(stake, pool, 1f)
                            val rate = TokenRateEntity(
                                currency = currency,
                                fiat = rates?.convert(token.address, stakeEntity.totalValue) ?: 0f,
                                rate = rates?.getRate(token.address) ?: 0f,
                                rateDiff24h = rates?.getDiff24h(token.address) ?: ""
                            )
                            verified.add(AccountTokenEntity(
                                balance = BalanceEntity(
                                    token = token,
                                    nano = stake.amountNano,
                                    walletAddress = accountId,
                                    stake = stakeEntity
                                ),
                                rate = rate
                            ))
                        } else {
                            balances.find { it.token.address == pool.liquidJettonMaster }?.let { balance ->
                                val tokenAddress = balance.token.address

                                val tonRate = ratesTon?.getRate(tokenAddress) ?: 1f

                                val stakeEntity = BalanceStakeEntity(stake, pool, tonRate).copy(amountNano = BigInteger.valueOf((balance.nano.toLong() * tonRate).toLong()))
                                val balanceEntity = balance.copy(stake = stakeEntity)

                                val rate = TokenRateEntity(
                                    currency = currency,
                                    fiat = rates?.convert(TokenEntity.TON.symbol, stakeEntity.totalValue) ?: 0f,
                                    rate = rates?.getRate(tokenAddress) ?: 0f,
                                    rateDiff24h = rates?.getDiff24h(tokenAddress) ?: ""
                                )

                                val token = AccountTokenEntity(
                                    balance = balanceEntity,
                                    rate = rate
                                )
                                processedTokens.add(token.address)
                                if (token.verified) {
                                    verified.add(token)
                                } else {
                                    unverified.add(token)
                                }
                            }
                        }
                    }
                }

                for (tokenAddress in pools.liquidJettons) {
                    if (processedTokens.contains(tokenAddress)) {
                        continue
                    }

                    val pool = pools.pools.map { it.value }.find { it.liquidJettonMaster == tokenAddress } ?: continue
                    val balance = balances.find { it.token.address == tokenAddress } ?: continue
                    if (balance.nano == BigInteger.ZERO) {
                        continue
                    }

                    val tonRate = ratesTon?.getRate(tokenAddress) ?: 1f

                    val stake = PoolStakeEntity(pool = pool.address, amountNano = BigInteger.ZERO, pendingWithdrawNano = BigInteger.ZERO, pendingDepositNano = BigInteger.ZERO, readyWithdrawNano = BigInteger.ZERO)
                    val stakeEntity = BalanceStakeEntity(stake, pool, tonRate).copy(amountNano = BigInteger.valueOf((balance.nano.toLong() * tonRate).toLong()))

                    val balanceEntity = balance.copy(stake = stakeEntity)
                    val rate = TokenRateEntity(
                        currency = currency,
                        fiat = rates?.convert(TokenEntity.TON.symbol, stakeEntity.totalValue) ?: 0f,
                        rate = rates?.getRate(tokenAddress) ?: 0f,
                        rateDiff24h = rates?.getDiff24h(tokenAddress) ?: ""
                    )

                    val token = AccountTokenEntity(
                        balance = balanceEntity,
                        rate = rate
                    )
                    processedTokens.add(token.address)
                    if (token.verified) {
                        verified.add(token)
                    } else {
                        unverified.add(token)
                    }
                }
            }


            for (balance in balances) {
                val tokenAddress = balance.token.address
                if (processedTokens.contains(tokenAddress) || balance.nano == BigInteger.ZERO && !balance.token.isTon) {
                    continue
                }
                val tokenRate = TokenRateEntity(
                    currency = currency,
                    fiat = rates?.convert(tokenAddress, balance.value) ?: 0f,
                    rate = rates?.getRate(tokenAddress) ?: 0f,
                    rateDiff24h = rates?.getDiff24h(tokenAddress) ?: ""
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
                    first.isTon && !first.isStake -> -1
                    second.isTon && !second.isStake -> 1
                    else -> second.fiat.compareTo(first.fiat)
                }
            }
        }

        private fun sortTestnet(list: List<AccountTokenEntity>): List<AccountTokenEntity> {
            return list.sortedWith { first, second ->
                when {
                    first.isTon && !first.isStake -> -1
                    second.isTon && !second.isStake -> 1
                    else -> second.balance.value.compareTo(first.balance.value)
                }
            }
        }
    }
}