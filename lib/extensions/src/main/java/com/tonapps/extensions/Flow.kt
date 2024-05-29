package com.tonapps.extensions

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

@Suppress("FunctionName", "unused")
fun <T> MutableEffectFlow(): MutableSharedFlow<T> {
    return MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
}

fun <T, R> Flow<List<T>>.mapList(transform: (T) -> R): Flow<List<R>> = map { list ->
    list.map(transform)
}

fun <T> Flow<List<T>>.filterList(predicate: (T) -> Boolean): Flow<List<T>> = map { list ->
    list.filter(predicate)
}