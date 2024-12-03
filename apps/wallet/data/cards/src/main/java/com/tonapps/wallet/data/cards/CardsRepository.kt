package com.tonapps.wallet.data.cards

import android.content.Context
import com.tonapps.blockchain.ton.contract.BaseWalletContract
import com.tonapps.blockchain.ton.extensions.toAccountId
import com.tonapps.blockchain.ton.extensions.toWalletAddress
import com.tonapps.blockchain.ton.proof.TONProof
import com.tonapps.extensions.MutableEffectFlow
import com.tonapps.icu.Coins
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.cards.entity.CardEntity
import com.tonapps.wallet.data.cards.entity.CardKind
import com.tonapps.wallet.data.cards.entity.CardsDataEntity
import com.tonapps.wallet.data.cards.entity.CardsList
import com.tonapps.wallet.data.cards.source.LocalDataSource
import com.tonapps.wallet.data.core.WalletCurrency
import com.tonapps.wallet.data.rates.RatesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import org.ton.block.AddrStd

class CardsRepository(
    context: Context,
    private val ratesRepository: RatesRepository,
    private val api: API,
) {

    private val localDataSource = LocalDataSource(context)

    private val _cardsUpdatedFlow = MutableEffectFlow<Unit>()
    val cardsUpdatedFlow = _cardsUpdatedFlow.asSharedFlow()

    init {
        _cardsUpdatedFlow.tryEmit(Unit)
    }

    suspend fun getCachedData(
        address: String,
        testnet: Boolean,
    ): CardsDataEntity = withContext(Dispatchers.IO) {
        localDataSource.getCards(address, testnet) ?: CardsDataEntity(
            accounts = emptyList(),
            prepaidCards = emptyList()
        )
    }

    suspend fun getCards(
        currency: WalletCurrency,
        address: String,
        testnet: Boolean,
        ignoreCache: Boolean = false,
    ): CardsList = withContext(Dispatchers.IO) {
        val cardsData = if (ignoreCache) {
            fetchAccounts(address, testnet)
        } else {
            localDataSource.getCards(address, testnet) ?: fetchAccounts(address, testnet)
        }

        val fiatRates = ratesRepository.getRates(
            currency,
            listOf(TokenEntity.TON.address, TokenEntity.USDT.address)
        )

        var totalFiat = Coins.ZERO

        val cards: MutableList<CardEntity> = cardsData.accounts.flatMap { account ->
            val tokenAddress =
                account.cryptoCurrency!!.tokenContract?.let { AddrStd(it).toAccountId() }
                    ?: TokenEntity.TON.address
            val balance = Coins.ofNano(account.balance, account.cryptoCurrency?.decimals ?: 9)
            val fiat = fiatRates.convert(tokenAddress, balance)
            totalFiat += fiat
            account.cards.map { card ->
                CardEntity(
                    type = CardEntity.Type.ACCOUNT,
                    id = card.id,
                    accountId = account.id,
                    balance = balance,
                    currency = account.cryptoCurrency?.ticker!!,
                    fiat = fiat,
                    lastFourDigits = card.lastFourDigits ?: "",
                    kind = CardKind.fromString(card.kind ?: ""),
                )
            }
        }.toMutableList()

        if (cardsData.prepaidCards.isNotEmpty()) {
            val tonRates = ratesRepository.getRates(
                cardsData.prepaidCards.map { WalletCurrency.of(it.fiatCurrency) }.distinct(),
                listOf(TokenEntity.TON.address)
            )


            cards.addAll(cardsData.prepaidCards.map { card ->
                val prepaidBalance = Coins.of(card.fiatBalance!!)
                val ton = tonRates[card.fiatCurrency]!!.convertFromFiat(
                    TokenEntity.TON.address,
                    prepaidBalance
                )
                val fiat = fiatRates.convertTON(ton)
                totalFiat += fiat
                CardEntity(
                    type = CardEntity.Type.PREPAID,
                    accountId = "",
                    id = card.id,
                    balance = ton,
                    currency = "TON",
                    fiat = fiat,
                    prepaidBalance = prepaidBalance,
                    prepaidCurrency = card.fiatCurrency,
                    lastFourDigits = card.lastFourDigits ?: "",
                    kind = CardKind.fromString(card.kind ?: ""),
                )
            })
        }

        CardsList(
            cards = cards.toList(),
            totalFiat = totalFiat
        )
    }

    private suspend fun fetchAccounts(
        token: String,
        address: String,
        testnet: Boolean
    ): CardsDataEntity = withContext(Dispatchers.IO) {
        try {
            val response = api.holdersApi.fetchAccountsList(token, testnet)
            val state = api.holdersApi.fetchUserState(token, testnet)

            val cardsDataEntity = CardsDataEntity(
                accounts = response.list,
                prepaidCards = response.prepaidCards,
                state = state
            )
            localDataSource.setCards(address, testnet, cardsDataEntity)
            _cardsUpdatedFlow.tryEmit(Unit)
            cardsDataEntity
        } catch (e: Exception) {
            CardsDataEntity(accounts = emptyList(), prepaidCards = emptyList())
        }
    }

    private suspend fun fetchAccounts(
        address: String,
        testnet: Boolean
    ): CardsDataEntity = withContext(Dispatchers.IO) {
        try {
            val token = getAccountToken(address, testnet)

            if (token !== null) {
                return@withContext fetchAccounts(token, address, testnet)
            }

            val accounts = api.holdersApi.fetchAccountsPublic(address, testnet)

            val cardsDataEntity = CardsDataEntity(accounts = accounts, prepaidCards = emptyList())
            localDataSource.setCards(address, testnet, cardsDataEntity)
            cardsDataEntity
        } catch (_: Exception) {
            CardsDataEntity(accounts = emptyList(), prepaidCards = emptyList())
        }
    }

    fun getAccountToken(
        address: String,
        testnet: Boolean
    ): String? = localDataSource.getAccountToken(address, testnet)

    suspend fun fetchAccountToken(
        contract: BaseWalletContract,
        proof: TONProof.Result,
        testnet: Boolean
    ): String? = withContext(Dispatchers.IO) {
        try {
            val token = api.holdersApi.fetchAccountToken(contract, proof, testnet)
            fetchAccounts(token, contract.address.toWalletAddress(testnet), testnet)
            localDataSource.setAccountToken(
                contract.address.toWalletAddress(testnet),
                testnet,
                token
            )

            token
        } catch (e: Exception) {
            null
        }
    }
}