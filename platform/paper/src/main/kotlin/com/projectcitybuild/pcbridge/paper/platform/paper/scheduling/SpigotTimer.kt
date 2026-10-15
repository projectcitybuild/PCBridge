
package com.projectcitybuild.pcbridge.paper.platform.paper.scheduling

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.paper.core.utils.Cancellable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.RejectedExecutionException
import kotlin.time.Duration
import kotlinx.coroutines.currentCoroutineContext

class SpigotTimer(
    plugin: JavaPlugin,
    private val errorTracker: ErrorTracker,
) {
    private val lock = Any()
    private val tasks = mutableMapOf<String, Job>()

    private val scopeJob = SupervisorJob()
    private val scope = CoroutineScope(plugin.asyncDispatcher + scopeJob)

    // Accessed only while holding lock.
    private var isShutdown = false

    fun scheduleOnce(
        identifier: String,
        delay: Duration,
        work: suspend () -> Unit,
    ): Cancellable {
        require(identifier.isNotBlank()) {
            "identifier must not be blank"
        }
        require(!delay.isNegative()) {
            "delay must not be negative"
        }
        return schedule(identifier) {
            if (delay.isPositive()) {
                delay(delay.inWholeMilliseconds)
            }
            work()
        }
    }

    fun scheduleRepeating(
        identifier: String,
        delay: Duration = Duration.ZERO,
        repeatingInterval: Duration,
        work: suspend () -> Unit,
    ): Cancellable {
        require(identifier.isNotBlank()) {
            "identifier must not be blank"
        }
        require(!delay.isNegative()) {
            "delay must not be negative"
        }
        require(repeatingInterval.isPositive()) {
            "repeatingInterval must be positive"
        }
        return schedule(identifier) {
            if (delay.isPositive()) {
                delay(delay.inWholeMilliseconds)
            }
            while (currentCoroutineContext().isActive) {
                try {
                    work()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    errorTracker.report(e)
                }
                delay(repeatingInterval.inWholeMilliseconds)
            }
        }
    }

    private fun schedule(
        identifier: String,
        work: suspend () -> Unit,
    ): Cancellable {
        // Lazy start lets us register the job before its body runs.
        val job = scope.launch(start = CoroutineStart.LAZY) {
            work()
        }
        synchronized(lock) {
            if (isShutdown) {
                job.cancel()
                throw RejectedExecutionException(
                    "Cannot schedule '$identifier': SpigotTimer is already shut down"
                )
            }
            tasks.remove(identifier)?.cancel()
            tasks[identifier] = job
        }

        job.invokeOnCompletion {
            synchronized(lock) {
                if (tasks[identifier] === job) {
                    tasks.remove(identifier)
                }
            }
        }

        job.start()

        return Cancellable {
            cancelJob(identifier, job)
        }
    }

    fun cancel(identifier: String) {
        val job = synchronized(lock) {
            tasks.remove(identifier)
        }
        job?.cancel()
    }

    private fun cancelJob(identifier: String, job: Job) {
        synchronized(lock) {
            if (tasks[identifier] === job) {
                tasks.remove(identifier)
            }
        }
        job.cancel()
    }

    /**
     * Cancels all currently scheduled jobs but allows future scheduling.
     */
    fun cancelAll() {
        val jobs = synchronized(lock) {
            tasks.values.toList().also {
                tasks.clear()
            }
        }

        jobs.forEach { it.cancel() }
    }

    /**
     * Permanently shuts down this timer.
     *
     * Safe to call more than once. Once shutdown starts, new schedules
     * are rejected, including schedules racing with this call.
     */
    fun shutdown() {
        val jobs = synchronized(lock) {
            if (isShutdown) return
            isShutdown = true

            tasks.values.toList().also {
                tasks.clear()
            }
        }

        jobs.forEach { it.cancel() }
        scope.cancel(
            CancellationException("SpigotTimer is shutting down")
        )
    }

    val isClosed: Boolean
        get() = synchronized(lock) {
            isShutdown
        }
}
