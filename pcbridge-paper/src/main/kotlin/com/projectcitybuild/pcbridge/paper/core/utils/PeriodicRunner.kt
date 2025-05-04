package com.projectcitybuild.pcbridge.paper.core.utils

import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.time.Duration

class PeriodicRunner(
    private val processInterval: Duration,
) {
    private var job: Job? = null
    private var jobId: UUID? = null
    private var action: (suspend () -> Unit)? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val running: Boolean
        get() = job?.isActive == true

    fun start(action: suspend () -> Unit) {
        val uuid = UUID.randomUUID()
        this.action = action
        this.jobId = uuid

        log.info { "Starting job queue (id: $uuid)" }

        job?.cancel()
        job = scope.launch {
            process(uuid)
        }
    }

    fun stop() {
        log.info { "Stopping job queue (id: $jobId)" }

        job?.cancel()
        job?.let {
            if (!it.isActive) {
                log.info { "Job ($jobId) has been successfully cancelled." }
            } else {
                log.warn { "Job ($jobId) cancellation failed or job was already inactive." }
            }
        }
        job = null
        jobId = null
    }

    private suspend fun process(jobId: UUID) {
        while (job?.isActive == true && this.jobId == jobId) {
            try {
                log.debug { "Executing task with jobId: $jobId" }
                action?.invoke()
            } catch (e: Exception) {
                log.error { "Failed to process runner action: ${e.message}" }
                e.printStackTrace()
            }
            kotlinx.coroutines.delay(processInterval)
        }
        log.info { "Job $jobId has been cancelled or completed." }
    }
}