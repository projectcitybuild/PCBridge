package com.projectcitybuild.entities

/**
 * Wraps a unit of work to be performed in the future that can be cancelled
 */
class AsyncCancellable<T> {
    private var listener: ((Result<T>) -> Void)? = null

    var cancelListener: (() -> Unit)? = null

    /**
     * Listens for the completion of the async task
     */
    fun subscribe(completion: (Result<T>) -> Void) {
        this.listener = completion
    }

    /**
     * Completes the async task
     */
    fun resolve(result: Result<T>) {
        listener?.let { it(result) }
    }

    /**
     * Stops observing the completion of the task
     */
    fun cancel() {
        cancelListener?.let { it() }

        this.listener = null
        this.cancelListener = null
    }
}