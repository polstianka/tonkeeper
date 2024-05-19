package com.tonapps.wallet.data.events.entities

import android.os.Parcelable
import com.tonapps.blockchain.Coin
import com.tonapps.wallet.api.entity.AccountEntity
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.collectibles.entities.NftEntity
import com.tonapps.wallet.data.events.ActionType
import io.tonapi.models.Action
import io.tonapi.models.ContractDeployAction
import io.tonapi.models.JettonSwapAction
import io.tonapi.models.JettonTransferAction
import io.tonapi.models.NftItemTransferAction
import io.tonapi.models.TonTransferAction
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class ActionEntity(
    val type: ActionType,
    val sender: AccountEntity? = null,
    val recipient: AccountEntity? = null,
    val comment: String? = null,
    val token: TokenEntity? = null,
    val amount: BigDecimal? = null,
    val nftAddress: String? = null,
    var nftEntity: NftEntity? = null
): Parcelable {

    companion object {
        fun map(
            actions: List<Action>,
            testnet: Boolean
        ): List<ActionEntity> {
            val list = mutableListOf<ActionEntity>()
            for (action in actions) {
                action.tonTransfer?.let {
                    list.add(mapTonTransferAction(it, testnet))
                }
                action.jettonTransfer?.let {
                    list.add(mapJettonTransferAction(it, testnet))
                }
                action.nftItemTransfer?.let {
                    list.add(mapNftTransferAction(it, testnet))
                }
                action.jettonSwap?.let {
                    list.add(mapJettonSwapAction(it))
                }
                action.contractDeploy?.let {
                    list.add(mapDeployContract(it))
                }
            }
            return list.toList()
        }

        private fun mapTonTransferAction(
            tonTransfer: TonTransferAction,
            testnet: Boolean
        ): ActionEntity {
            return ActionEntity(
                type = ActionType.TonTransfer,
                sender = AccountEntity(tonTransfer.sender, testnet),
                recipient = AccountEntity(tonTransfer.recipient, testnet),
                comment = tonTransfer.comment,
                token = TokenEntity.TON,
                amount = Coin.toCoins(tonTransfer.amount)
            )
        }

        private fun mapJettonTransferAction(
            jettonTransfer: JettonTransferAction,
            testnet: Boolean
        ): ActionEntity {
            return ActionEntity(
                type = ActionType.JettonTransfer,
                sender = jettonTransfer.sender?.let { AccountEntity(it, testnet) },
                recipient = jettonTransfer.recipient?.let { AccountEntity(it, testnet) },
                comment = jettonTransfer.comment,
                token = TokenEntity(jettonTransfer.jetton),
                amount = Coin.parseJettonBalance(jettonTransfer.amount, jettonTransfer.jetton.decimals)
            )
        }

        private fun mapJettonSwapAction(
            jettonSwap: JettonSwapAction
        ): ActionEntity {
            return ActionEntity(
                type = ActionType.JettonSwap,
            )
        }

        private fun mapDeployContract(
            deployContract: ContractDeployAction
        ): ActionEntity {
            return ActionEntity(
                type = ActionType.DeployContract,
            )
        }

        private fun mapNftTransferAction(
            nftTransfer: NftItemTransferAction,
            testnet: Boolean
        ): ActionEntity {
            return ActionEntity(
                type = ActionType.NftTransfer,
                sender = nftTransfer.sender?.let { AccountEntity(it, testnet) },
                recipient = nftTransfer.recipient?.let { AccountEntity(it, testnet) },
                comment = nftTransfer.comment,
                nftAddress = nftTransfer.nft,
            )
        }
    }
}