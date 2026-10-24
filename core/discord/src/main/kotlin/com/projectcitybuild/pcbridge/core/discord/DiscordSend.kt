package com.projectcitybuild.pcbridge.core.discord

import com.projectcitybuild.pcbridge.core.localconfig.LocalConfig
import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.core.observability.errors.reportSafely
import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import com.projectcitybuild.pcbridge.core.pcbridgeapi.discord.models.DiscordEmbed
import com.projectcitybuild.pcbridge.core.pcbridgeapi.discord.services.DiscordHttpService
import com.projectcitybuild.pcbridge.core.utils.PeriodicRunner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

class DiscordSend(
    private val localConfig: LocalConfig,
    private val discordHttpService: DiscordHttpService,
    private val errorTracker: ErrorTracker,
    private val periodicRunner: PeriodicRunner,
) {
    private val queue = mutableListOf<DiscordEmbed>()
    private val shutdownMutex = Mutex()
    private var enabled = true

    @Volatile
    private var closed = false

    init {
        if (localConfig.get().discord.contentAlertWebhook.isEmpty()) {
            logSync.warn { "No webhook configured for content alerts. No messages will be sent to Discord" }
            enabled = false
        }
    }

    fun send(embed: DiscordEmbed) {
        if (!enabled) {
            logSync.debug { "Skipping Discord embed queue" }
            return
        }
        val accepted = synchronized(queue) {
            if (closed) {
                false
            } else {
                queue.add(embed)
                true
            }
        }
        if (!accepted) {
            logSync.debug { "Skipping Discord embed queue" }
            return
        }
        logSync.trace { "Queuing Discord embed: $embed" }

        if (!periodicRunner.running) {
            periodicRunner.start(::processRequests)
        }
    }

    suspend fun shutdown() = shutdownMutex.withLock {
        synchronized(queue) { closed = true }
        periodicRunner.shutdown()

        withTimeout(5.seconds) {
            repeat(3) { attempt ->
                if (synchronized(queue) { queue.isEmpty() }) return@withTimeout
                try {
                    processRequests()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    if (attempt == 2) throw e
                    delay(250L * (attempt + 1))
                }
            }
            check(synchronized(queue) { queue.isEmpty() }) { "Discord queue did not drain before shutdown" }
        }
    }

    private suspend fun processRequests() {
        // Discord only supports up to 10 embeds in a single message.
        val batch = synchronized(queue) {
            queue.take(10).also { queue.subList(0, it.size).clear() }
        }

        if (batch.isNotEmpty()) {
            log.trace { "Sending Discord embed batch of size ${batch.size}" }
            try {
                sendMessage(batch)
            } catch (e: Throwable) {
                synchronized(queue) { queue.addAll(0, batch) }
                throw e
            }
        }

        if (synchronized(queue) { queue.isEmpty() }) {
            periodicRunner.stop()
        }
    }

    private suspend fun sendMessage(embeds: List<DiscordEmbed>) {
        try {
            val config = localConfig.get().discord
            val webhookUrl = config.contentAlertWebhook
            discordHttpService.executeWebhook(webhookUrl, embeds)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.error(e) { "Failed to send Discord message" }
            errorTracker.reportSafely(e)
            throw e
        }
    }
}
