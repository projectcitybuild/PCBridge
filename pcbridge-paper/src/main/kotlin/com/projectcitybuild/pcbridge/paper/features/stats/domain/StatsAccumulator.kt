package com.projectcitybuild.pcbridge.paper.features.stats.domain

import com.projectcitybuild.pcbridge.http.discord.models.DiscordEmbed
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.core.utils.PeriodicRunner
import java.util.UUID

class StatsAccumulator(
    private val periodicRunner: PeriodicRunner,
) {
    private val placed = mutableMapOf<UUID, Long>()
    private val destroyed = mutableMapOf<UUID, Long>()
    private val travelled = mutableMapOf<UUID, Long>()

    suspend fun blockPlaced(uuid: UUID) {
        val current = placed.getOrDefault(uuid, defaultValue = 0)
        placed[uuid] = current + 1
    }

    suspend fun blockDestroyed(uuid: UUID) {
        val current = destroyed.getOrDefault(uuid, defaultValue = 0)
        destroyed[uuid] = current + 1
    }

    suspend fun travelled(uuid: UUID, distance: Long) {
        val current = travelled.getOrDefault(uuid, defaultValue = 0)
        travelled[uuid] = current + distance
    }

    private val queue = mutableListOf<DiscordEmbed>()

    fun send(embed: DiscordEmbed) {
        logSync.trace { "Queuing Discord embed: $embed" }
        queue.add(embed)

        if (!periodicRunner.running) {
            periodicRunner.start(::processRequests)
        }
    }

    private suspend fun processRequests() {
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
            log.error(e) { "Failed to send Discord message" }
            e.printStackTrace()
            errorTracker.report(e)
        }
    }
}