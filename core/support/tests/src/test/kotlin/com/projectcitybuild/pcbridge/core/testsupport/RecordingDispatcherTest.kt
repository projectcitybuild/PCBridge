package com.projectcitybuild.pcbridge.core.testsupport

import kotlinx.coroutines.CoroutineDispatcher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class RecordingDispatcherTest {
    @Test
    fun `dispatch should count and forward work to delegate`() {
        val calls = mutableListOf<String>()
        val delegate = object : CoroutineDispatcher() {
            override fun dispatch(
                context: CoroutineContext,
                block: Runnable,
            ) {
                calls.add("delegate")
                block.run()
            }
        }
        val dispatcher = RecordingDispatcher(delegate)

        dispatcher.dispatch(EmptyCoroutineContext, Runnable { calls.add("block") })

        assertEquals(1, dispatcher.dispatchCount)
        assertEquals(listOf("delegate", "block"), calls)
    }
}
