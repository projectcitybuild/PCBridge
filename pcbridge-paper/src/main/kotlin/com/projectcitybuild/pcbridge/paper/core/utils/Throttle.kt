package com.projectcitybuild.pcbridge.paper.core.utils

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class Throttle {
    private var lastExecutionTime = Duration.ZERO
    private var isWaiting = false

    /**
     * Throttles the execution of the provided [action]. Ensures that the action
     * is invoked at most once every [interval].
     */
    suspend fun attempt(interval: Duration, action: suspend () -> Unit) {
        val currentTime = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS)

        if (!isWaiting && currentTime - lastExecutionTime >= interval) {
            isWaiting = true
            try {
                action()
                lastExecutionTime = currentTime
            } finally {
                isWaiting = false
            }
        }
    }
}