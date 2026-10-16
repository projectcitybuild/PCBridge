package com.projectcitybuild.pcbridge.core.testsupport

import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.CoroutineContext

class RecordingDispatcher(
    private val delegate: CoroutineDispatcher,
) : CoroutineDispatcher() {
    var dispatchCount = 0
        private set

    override fun dispatch(
        context: CoroutineContext,
        block: Runnable,
    ) {
        dispatchCount++
        delegate.dispatch(context, block)
    }
}