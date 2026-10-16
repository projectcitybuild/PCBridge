package com.projectcitybuild.pcbridge.platform.paper.scheduling

import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.core.observability.errors.SentryProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
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
import java.util.concurrent.RejectedExecutionException
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class SpigotTimerTest {
    @Test
    fun `schedule once should run after delay`() =
        runTest {
            val timer = createTimer(StandardTestDispatcher(testScheduler))
            var calls = 0

            timer.scheduleOnce("once", 2.seconds) { calls++ }

            runCurrent()
            assertEquals(0, calls)

            advanceTimeBy(2.seconds)
            runCurrent()
            assertEquals(1, calls)

            timer.shutdown()
        }

    @Test
    fun `schedule once should not run after cancellation`() =
        runTest {
            val timer = createTimer(StandardTestDispatcher(testScheduler))
            var calls = 0
            val cancellable = timer.scheduleOnce("once", 1.seconds) { calls++ }

            cancellable.cancel()
            runCurrent()
            advanceTimeBy(1.seconds)
            runCurrent()

            assertEquals(0, calls)
            assertTrue(cancellable.isCancelled)
            timer.shutdown()
        }

    @Test
    fun `cancel by identifier should stop active work`() =
        runTest {
            val timer = createTimer(StandardTestDispatcher(testScheduler))
            var workWasCancelled = false

            timer.scheduleOnce("active", 0.seconds) {
                try {
                    awaitCancellation()
                } finally {
                    workWasCancelled = true
                }
            }
            runCurrent()
            assertFalse(workWasCancelled)

            timer.cancel("active")
            runCurrent()

            assertTrue(workWasCancelled)
            timer.shutdown()
        }

    @Test
    fun `scheduling with same identifier should replace previous work`() =
        runTest {
            val timer = createTimer(StandardTestDispatcher(testScheduler))
            var firstWasCancelled = false
            var secondCalls = 0

            timer.scheduleOnce("shared", 0.seconds) {
                try {
                    awaitCancellation()
                } finally {
                    firstWasCancelled = true
                }
            }
            runCurrent()

            timer.scheduleOnce("shared", 0.seconds) { secondCalls++ }
            runCurrent()

            assertTrue(firstWasCancelled)
            assertEquals(1, secondCalls)
            timer.shutdown()
        }

    @Test
    fun `cancel all should cancel scheduled work and allow new schedules`() =
        runTest {
            val timer = createTimer(StandardTestDispatcher(testScheduler))
            var firstWasCancelled = false
            var laterCalls = 0

            timer.scheduleOnce("first", 0.seconds) {
                try {
                    awaitCancellation()
                } finally {
                    firstWasCancelled = true
                }
            }
            runCurrent()

            timer.cancelAll()
            runCurrent()

            timer.scheduleOnce("later", 0.seconds) { laterCalls++ }
            runCurrent()

            assertTrue(firstWasCancelled)
            assertEquals(1, laterCalls)
            timer.shutdown()
        }

    @Test
    fun `repeating work should report errors and continue on interval`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val sentry = mock<SentryProvider>()
            val timer = createTimer(dispatcher, ErrorTracker(sentry))
            val failure = IllegalStateException("work failed")
            var calls = 0

            timer.scheduleRepeating(
                identifier = "repeating",
                delay = 500.milliseconds,
                repeatingInterval = 1.seconds,
            ) {
                calls++
                if (calls == 1) throw failure
            }

            runCurrent()
            assertEquals(0, calls)
            advanceTimeBy(500.milliseconds)
            runCurrent()
            assertEquals(1, calls)

            advanceTimeBy(1.seconds)
            runCurrent()
            assertEquals(2, calls)
            verify(sentry).report(failure)

            timer.shutdown()
        }

    @Test
    fun `shutdown should cancel work and reject new schedules`() =
        runTest {
            val timer = createTimer(StandardTestDispatcher(testScheduler))
            var workWasCancelled = false
            timer.scheduleOnce("active", 0.seconds) {
                try {
                    awaitCancellation()
                } finally {
                    workWasCancelled = true
                }
            }
            runCurrent()

            timer.shutdown()
            timer.shutdown()
            runCurrent()

            assertTrue(timer.isClosed)
            assertTrue(workWasCancelled)
            assertThrows(RejectedExecutionException::class.java) {
                timer.scheduleOnce("rejected", 0.seconds) {}
            }
        }

    @Test
    fun `schedule methods should reject invalid arguments`() {
        val timer = createTimer()

        assertThrows(IllegalArgumentException::class.java) {
            timer.scheduleOnce(" ", 0.seconds) {}
        }
        assertThrows(IllegalArgumentException::class.java) {
            timer.scheduleOnce("negative-delay", (-1).seconds) {}
        }
        assertThrows(IllegalArgumentException::class.java) {
            timer.scheduleRepeating("negative-delay", (-1).seconds, 1.seconds) {}
        }
        assertThrows(IllegalArgumentException::class.java) {
            timer.scheduleRepeating("zero-interval", repeatingInterval = 0.seconds) {}
        }
        assertThrows(IllegalArgumentException::class.java) {
            timer.scheduleRepeating("negative-interval", repeatingInterval = (-1).seconds) {}
        }
        assertFalse(timer.isClosed)

        timer.shutdown()
    }

    private fun createTimer(
        context: CoroutineContext = Dispatchers.Unconfined,
        errorTracker: ErrorTracker =
            ErrorTracker(
                SentryProvider("", "", null),
            ),
    ): SpigotTimer {
        return SpigotTimer(
            mock<JavaPlugin>(),
            context,
            errorTracker,
        )
    }
}
