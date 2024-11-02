package com.projectcitybuild.pcbridge.paper.core.discord.services

import com.projectcitybuild.pcbridge.http.services.discord.DiscordHttpService
import com.projectcitybuild.pcbridge.paper.core.localconfig.LocalConfig

class DiscordSend(
    private val localConfig: LocalConfig,
    private val discordHttpService: DiscordHttpService,
) {
    suspend fun send(content: String) {
        // TODO: batching
        val config = localConfig.get().discord
        val webhookUrl = config.contentAlertWebhook
        discordHttpService.executeWebhook(webhookUrl, content)
    }
}