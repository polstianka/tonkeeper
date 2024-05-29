package com.tonapps.wallet.data.core.api

interface Repository<Req: Request, Res> {
    fun get(req: Req): ApiDataRepository.Result<Res>?
}