package com.projectcitybuild.core.utilities

import com.projectcitybuild.entities.Result
import java.util.concurrent.locks.ReentrantLock

private typealias CompletionHandler<T> = (T) -> Void

/**
 * Wraps a unit of work to be performed in the future that can be cancelled
 */
class AsyncTask<T>(private val task: (CompletionHandler<T>) -> Cancellable) {
    private val lock = ReentrantLock()

    var isExecuting: Boolean = false
        private set

    var isCompleted: Boolean = false
        private set

    /**
     * Listens for the completion of the async task
     */
    fun startAndSubscribe(completion: CompletionHandler<T>): Cancellable {
        lock.lock()
        try {
            if (isExecuting) throw Exception("Task has already been started")

            isExecuting = true
            return task { taskResult ->
                if (isCompleted) throw Exception("Attempted to complete twice")

                isCompleted = true
                completion(taskResult)
            }
        }
        finally {
            lock.unlock()
        }
    }
}