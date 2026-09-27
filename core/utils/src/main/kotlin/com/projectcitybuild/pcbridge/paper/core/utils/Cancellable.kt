package com.projectcitybuild.pcbridge.paper.core.utils

import java.util.concurrent.locks.ReentrantLock

/**
 * Thread-safe wrapper for anything that can be cancelled
 */
class Cancellable(private val handler: () -> Unit) {
    private val lock = ReentrantLock()

    var isCancelled: Boolean = false
        private set

    fun cancel() {
        lock.lock()
        try {
            if (isCancelled) return

            handler()
            isCancelled = true
        } finally {
            lock.unlock()
        }
    }
}
