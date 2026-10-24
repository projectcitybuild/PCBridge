package com.projectcitybuild.pcbridge.core.utils

import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.util.UUID
import kotlin.time.Duration

class PeriodicRunner(
    private val processInterval: Duration,
) {
    private var job: Job? = null
    private var jobId: UUID? = null
    private var action: (suspend () -> Unit)? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isClosed = false

    val running: Boolean
        get() = job?.isActive == true

    fun start(action: suspend () -> Unit) {
        check(!isClosed) { "PeriodicRunner is already shut down" }
        val uuid = UUID.randomUUID()
        this.action = action
        this.jobId = uuid

        logSync.debug { "Starting job queue (id: $uuid)" }

        job?.cancel()
        val newJob = scope.launch(start = CoroutineStart.LAZY) {
            process(uuid)
        }
        job = newJob
        newJob.start()
    }

    fun stop() {
        logSync.debug { "Stopping job queue (id: $jobId)" }

        val previous = job
        previous?.cancel()
        job = null
        jobId = null
    }

    suspend fun stopAndJoin() {
        val previous = job
        stop()
        previous?.join()
        if (previous != null) logSync.info { "Job ($jobId) has been successfully cancelled." }
    }

    suspend fun shutdown() {
        if (isClosed) return
        stopAndJoin()
        isClosed = true
        scope.cancel()
    }

    private suspend fun process(jobId: UUID) {
        while (job?.isActive == true && this.jobId == jobId) {
            try {
                log.debug { "Executing task with jobId: $jobId" }
                action?.invoke()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.error(e) { "Failed to process runner action" }
            }
            delay(processInterval)
        }
        log.debug { "Job $jobId has been cancelled or completed." }
    }
}
