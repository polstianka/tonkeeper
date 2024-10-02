package com.tonapps.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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

@OptIn(FlowPreview::class)
fun <T> Flow<T>.single(timeout: Duration = 1.seconds): Flow<T> {
    return this.take(1).timeout(timeout)
}

@OptIn(FlowPreview::class)
suspend fun <T> Flow<T>.singleValue(timeout: Duration = 1.seconds): T? {
    return this.take(1).timeout(timeout).firstOrNull()
}

fun <T> Flow<T>.state(
    scope: CoroutineScope,
    context: CoroutineContext = Dispatchers.IO
): Flow<T & Any> {
    return flowOn(context).stateIn(scope, SharingStarted.Eagerly, null).filterNotNull()
}

fun <T> Flow<Flow<T>>.flattenFirst(): Flow<T> = channelFlow {
    val busy = AtomicBoolean(false)

    collect { inner ->
        if (busy.compareAndSet(false, true)) {
            launch(start = CoroutineStart.UNDISPATCHED) {
                try {
                    inner.collect { send(it) }
                    busy.set(false)
                } catch (e: CancellationException) {
                    busy.set(false)
                }
            }
        }
    }
}

fun <T> join(vararg flows: Flow<T>): Flow<T> = channelFlow<Result<T>> {
    val jobs = flows.map { flow ->
        launch {
            flow.buffer(Channel.BUFFERED).onEach {
                value -> send(Result.success(value))
            }.catch {
                error -> send(Result.failure(error))
            }.collect()
        }
    }
    awaitClose {
        jobs.forEach { it.cancel() }
    }
}.map { it.getOrThrow() }


@OptIn(ExperimentalCoroutinesApi::class)
inline fun <T, R> Flow<T>.flat(crossinline transform: suspend (value: T) -> List<Flow<R>>): Flow<R> {
    return flatMapLatest { value ->
        val flows = transform(value).toTypedArray()
        join(*flows)
    }
}