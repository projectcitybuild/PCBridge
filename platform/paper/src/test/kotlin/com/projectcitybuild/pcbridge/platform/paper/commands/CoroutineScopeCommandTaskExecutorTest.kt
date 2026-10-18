package com.projectcitybuild.pcbridge.platform.paper.commands

import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CoroutineScopeCommandTaskExecutorTest {
    @Test
    fun `runs command task on the injected coroutine scope`() =
        runTest {
            var executions = 0
            val executor = CoroutineScopeCommandTaskExecutor(this)

            executor.execute {
                executions++
            }

            assertEquals(0, executions)
            runCurrent()
            assertEquals(1, executions)
        }
}
