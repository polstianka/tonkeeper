package com.tonapps.tonkeeper.core.history

enum class ActionType {
    Received,
    Send,
    CallContract,
    NftReceived,
    NftSend,
    Swap,
    DeployContract,
    DepositStake,
    JettonMint,
    AuctionBid,
    WithdrawStakeRequest,
    WithdrawStake,
    DomainRenewal,
    Unknown,
    NftPurchase,
    JettonBurn,
    UnSubscribe,
    Subscribe,
    Fee,
}