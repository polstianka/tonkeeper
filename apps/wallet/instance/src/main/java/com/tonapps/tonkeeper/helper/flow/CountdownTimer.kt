package com.tonapps.tonkeeper.helper.flow

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import java.time.Duration
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

object CountdownTimer {

    fun remaining(timestamp: Long): Long {
        val targetTime = Instant.ofEpochSecond(timestamp)
        val now = Instant.now()
        return Duration.between(now, targetTime).seconds
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun create(timestampFlow: Flow<Long>): Flow<Long> {
        return timestampFlow.flatMapLatest { timestamp ->
            flow {
                val targetTime = Instant.ofEpochSecond(timestamp)
                while (true) {
                    val now = Instant.now()
                    val remainingSeconds = Duration.between(now, targetTime).seconds
                    if (remainingSeconds <= 0) {
                        emit(0)
                        break
                    } else {
                        emit(remainingSeconds)
                        delay(1.seconds)
                    }
                }
            }
        }
    }

    fun format(remainingSeconds: Long): String {
        val hours = remainingSeconds / 3600
        val minutes = (remainingSeconds % 3600) / 60
        val seconds = remainingSeconds % 60
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }
}