package com.projectcitybuild.pcbridge.platform.paper.scheduling

import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import kotlin.time.Duration.Companion.milliseconds
import java.util.concurrent.RejectedExecutionException

class SpigotTimerTest {
    private fun createTimer(
        dispatcher: StandardTestDispatcher,
        errorTracker: ErrorTracker = mock(),
    ): SpigotTimer =
        SpigotTimer(
            plugin = mock<JavaPlugin>(),
            errorTracker = errorTracker,
            context = dispatcher,
        )

    @Test
    fun `scheduleOnce runs after the requested delay`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timer = createTimer(dispatcher)
        var executions = 0

        timer.scheduleOnce(
            identifier = "once",
            delay = 100.milliseconds,
        ) {
            executions++
        }

        runCurrent()
        assertEquals(0, executions)

        advanceTimeBy(99)
        runCurrent()
        assertEquals(0, executions)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(1, executions)

        timer.shutdown()
    }

    @Test
    fun `scheduleRepeating executes at the configured interval`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timer = createTimer(dispatcher)
        var executions = 0

        val task = timer.scheduleRepeating(
            identifier = "repeating",
            repeatingInterval = 100.milliseconds,
        ) {
            executions++
        }

        runCurrent()
        assertEquals(1, executions)

        advanceTimeBy(100)
        runCurrent()
        assertEquals(2, executions)

        advanceTimeBy(100)
        runCurrent()
        assertEquals(3, executions)

        task.cancel()
        runCurrent()

        advanceTimeBy(500)
        runCurrent()
        assertEquals(3, executions)

        timer.shutdown()
    }

    @Test
    fun `repeating task reports work exception and continues`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val errorTracker = mock<ErrorTracker>()
        val timer = createTimer(dispatcher, errorTracker)
        val failure = IllegalStateException("simulated failure")
        var executions = 0

        val task = timer.scheduleRepeating(
            identifier = "failure-test",
            repeatingInterval = 100.milliseconds,
        ) {
            executions++
            if (executions == 1) {
                throw failure
            }
        }

        runCurrent()

        assertEquals(1, executions)
        verify(errorTracker).report(failure)

        advanceTimeBy(100)
        runCurrent()

        assertEquals(2, executions)

        task.cancel()
        timer.shutdown()
    }

    @Test
    fun `cancelling a task stops future execution`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timer = createTimer(dispatcher)
        var executions = 0

        val task = timer.scheduleRepeating(
            identifier = "cancel-me",
            repeatingInterval = 100.milliseconds,
        ) {
            executions++
        }

        runCurrent()
        assertEquals(1, executions)

        task.cancel()
        runCurrent()

        advanceTimeBy(500)
        runCurrent()

        assertEquals(1, executions)
        assertTrue(task.isCancelled)

        timer.shutdown()
    }

    @Test
    fun `scheduling with same identifier replaces previous task`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timer = createTimer(dispatcher)
        var firstExecutions = 0
        var secondExecutions = 0

        val first = timer.scheduleRepeating(
            identifier = "shared",
            repeatingInterval = 100.milliseconds,
        ) {
            firstExecutions++
        }

        runCurrent()
        assertEquals(1, firstExecutions)

        timer.scheduleRepeating(
            identifier = "shared",
            repeatingInterval = 100.milliseconds,
        ) {
            secondExecutions++
        }

        runCurrent()
        assertEquals(1, firstExecutions)
        assertEquals(1, secondExecutions)
        assertTrue(first.isCancelled)

        advanceTimeBy(100)
        runCurrent()

        assertEquals(1, firstExecutions)
        assertEquals(2, secondExecutions)

        timer.shutdown()
    }

    @Test
    fun `cancelAll cancels current tasks but allows future scheduling`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timer = createTimer(dispatcher)
        var firstExecutions = 0
        var secondExecutions = 0

        timer.scheduleRepeating(
            identifier = "first",
            repeatingInterval = 100.milliseconds,
        ) {
            firstExecutions++
        }

        runCurrent()
        assertEquals(1, firstExecutions)

        timer.cancelAll()
        runCurrent()

        advanceTimeBy(200)
        runCurrent()
        assertEquals(1, firstExecutions)

        timer.scheduleOnce(
            identifier = "second",
            delay = 0.milliseconds,
        ) {
            secondExecutions++
        }

        runCurrent()
        assertEquals(1, secondExecutions)

        timer.shutdown()
    }

    @Test
    fun `shutdown is idempotent and rejects new tasks`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timer = createTimer(dispatcher)

        timer.shutdown()
        timer.shutdown()

        assertTrue(timer.isClosed)

        assertThrows(
            RejectedExecutionException::class.java,
        ) {
            timer.scheduleOnce(
                identifier = "after-shutdown",
                delay = 0.milliseconds,
            ) {}
        }
    }

    @Test
    fun `zero repeating interval is rejected`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timer = createTimer(dispatcher)

        assertThrows(IllegalArgumentException::class.java) {
            timer.scheduleRepeating(
                identifier = "invalid",
                repeatingInterval = 0.milliseconds,
            ) {}
        }

        timer.shutdown()
    }

    @Test
    fun `timer is not closed before shutdown`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timer = createTimer(dispatcher)

        assertFalse(timer.isClosed)

        timer.shutdown()

        assertTrue(timer.isClosed)
    }
}
