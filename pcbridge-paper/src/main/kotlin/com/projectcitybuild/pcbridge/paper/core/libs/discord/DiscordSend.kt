package com.projectcitybuild.pcbridge.paper.core.libs.discord

import com.projectcitybuild.pcbridge.http.discord.services.DiscordHttpService
import com.projectcitybuild.pcbridge.http.discord.models.DiscordEmbed
import com.projectcitybuild.pcbridge.paper.core.libs.errors.SentryReporter
import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.LocalConfig
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.core.utils.PeriodicRunner

class DiscordSend(
    private val localConfig: LocalConfig,
    private val discordHttpService: DiscordHttpService,
    private val sentryReporter: SentryReporter,
    private val periodicRunner: PeriodicRunner,
) {
    private val queue = mutableListOf<DiscordEmbed>()
    private var enabled = true

    init {
        if (localConfig.get().discord.contentAlertWebhook.isEmpty()) {
            log.warn { "No webhook configured for content alerts. No messages will be sent to Discord" }
            enabled = false
        }
    }

    fun send(embed: DiscordEmbed) {
        if (!enabled) {
            log.debug { "Skipping Discord embed queue" }
            return
        }
        log.trace { "Queuing Discord embed: $embed" }
        queue.add(embed)

        if (!periodicRunner.running) {
            periodicRunner.start(::processRequests)
        }
    }

    private suspend fun processRequests() {
        // Discord only supports up to 10 embeds in a single message
        // See https://discord.com/developers/docs/resources/webhook#execute-webhook
        val batch = queue.take(10).toList()
        queue.removeAll(batch)

        if (batch.isNotEmpty()) {
            log.trace { "Sending Discord embed batch of size ${batch.size}" }
            sendMessage(batch)
        }

        if (queue.isEmpty()) {
            periodicRunner.stop()
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