package com.projectcitybuild.pcbridge.paper.core.libs.services

import com.projectcitybuild.pcbridge.http.services.discord.DiscordHttpService
import com.projectcitybuild.pcbridge.http.models.discord.DiscordEmbed
import com.projectcitybuild.pcbridge.paper.core.libs.errors.SentryReporter
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.LocalConfig
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class DiscordSend(
    private val localConfig: LocalConfig,
    private val discordHttpService: DiscordHttpService,
    private val sentryReporter: SentryReporter,
    private val delay: Duration = 10.seconds,
) {
    private val queue = mutableListOf<DiscordEmbed>()
    private var job: Job? = null

    fun startProcessing() {
        log.info { "Starting Discord message queue" }

        if (localConfig.get().discord.contentAlertWebhook.isEmpty()) {
            log.warn { "No webhook configured for content alerts. No messages will be sent to Discord" }
            return
        }

        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            processRequests()
        }
    }

    fun stopProcessing() {
        log.info { "Stopping Discord message queue" }

        job?.cancel()
    }

    fun send(embed: DiscordEmbed) {
        if (job == null) {
            log.debug { "Skipping Discord embed queue" }
            return
        }
        log.trace { "Queuing Discord embed: $embed" }
        queue.add(embed)
    }

    private suspend fun processRequests() {
        while (true) {
            // Discord only supports up to 10 embeds in a single message
            // See https://discord.com/developers/docs/resources/webhook#execute-webhook
            val batch = queue.take(10).toList()
            queue.removeAll(batch)

            if (batch.isNotEmpty()) {
                log.trace { "Sending Discord embed batch of size ${batch.size}" }
                sendMessage(batch)
            }
            delay(delay.toJavaDuration())
        }
    }

    private suspend fun sendMessage(embeds: List<DiscordEmbed>) {
        try {
            val config = localConfig.get().discord
            val webhookUrl = config.contentAlertWebhook
            discordHttpService.executeWebhook(webhookUrl, embeds)
        } catch (e: Exception) {
            log.error { "Failed to send Discord message: ${e.message}" }
            e.printStackTrace()
            sentryReporter.report(e)
        }
    }
}