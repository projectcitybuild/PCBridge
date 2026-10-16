package com.projectcitybuild.pcbridge.core.utils

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class PeriodicRunnerTest {
    @Test
    fun `start should run action repeatedly until stopped`() =
        runBlocking {
            val runner = PeriodicRunner(10.milliseconds)
            val runCount = AtomicInteger()
            val secondRun = CompletableDeferred<Unit>()

            runner.start {
                if (runCount.incrementAndGet() == 2) {
                    secondRun.complete(Unit)
                }
            }

            try {
                withTimeout(1.seconds) {
                    secondRun.await()
                }
                assertTrue(runner.running)
            } finally {
                runner.stop()
            }

            val stoppedAt = runCount.get()
            delay(50.milliseconds)

            assertFalse(runner.running)
            assertEquals(stoppedAt, runCount.get())
        }

    @Test
    fun `starting again should cancel previous action`() =
        runBlocking {
            val runner = PeriodicRunner(10.milliseconds)
            val firstStarted = CompletableDeferred<Unit>()
            val firstCancelled = CompletableDeferred<Unit>()
            val secondStarted = CompletableDeferred<Unit>()

            runner.start {
                firstStarted.complete(Unit)
                try {
                    awaitCancellation()
                } finally {
                    firstCancelled.complete(Unit)
                }
            }

            try {
                withTimeout(1.seconds) {
                    firstStarted.await()
                }
                runner.start {
                    secondStarted.complete(Unit)
                    awaitCancellation()
                }
                withTimeout(1.seconds) {
                    firstCancelled.await()
                    secondStarted.await()
                }

                assertTrue(runner.running)
            } finally {
                runner.stop()
            }

            assertFalse(runner.running)
        }
}
