package com.tonapps.tonkeeper.api.base

data class RepositoryResponse<D>(
    val source: Source,
    val data: D
) {

    enum class Source {
        MEMORY, CACHE, CLOUD
    }

    companion object {

        fun <D> memory(data: D) = RepositoryResponse(Source.MEMORY, data)

        fun <D> cache(data: D) = RepositoryResponse(Source.CACHE, data)

        fun <D> cloud(data: D) = RepositoryResponse(Source.CLOUD, data)
    }
}