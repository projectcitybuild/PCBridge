package com.projectcitybuild.pcbridge.core.discord

import com.projectcitybuild.pcbridge.core.localconfig.LocalConfig
import com.projectcitybuild.pcbridge.core.localconfig.LocalConfigBacking
import com.projectcitybuild.pcbridge.core.localconfig.LocalConfigKeyValues
import com.projectcitybuild.pcbridge.core.localconfig.default
import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.core.observability.errors.SentryProvider
import com.projectcitybuild.pcbridge.core.pcbridgeapi.discord.models.DiscordEmbed
import com.projectcitybuild.pcbridge.core.pcbridgeapi.discord.services.DiscordHttpService
import com.projectcitybuild.pcbridge.core.utils.PeriodicRunner
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File

class DiscordSendTest {
    @Test
    fun `send should drain queued embeds in batches of ten`() =
        runBlocking {
            var runnerIsRunning = false
            var processQueue: (suspend () -> Unit)? = null
            val runner = mock<PeriodicRunner>()
            whenever(runner.running).thenAnswer { runnerIsRunning }
            doAnswer { invocation ->
                runnerIsRunning = true
                processQueue = invocation.getArgument(0)
                null
            }.whenever(runner).start(any())
            doAnswer {
                runnerIsRunning = false
                null
            }.whenever(runner).stop()

            val webhookUrl = "https://discord.example.test/webhook"
            val service = mock<DiscordHttpService>()
            val sender = DiscordSend(
                localConfig(webhookUrl),
                service,
                ErrorTracker(mock<SentryProvider>()),
                runner,
            )
            val embeds = (1..12).map { DiscordEmbed(title = "Embed $it") }

            embeds.forEach(sender::send)
            assertNotNull(processQueue)
            assertTrue(runnerIsRunning)

            processQueue!!.invoke()
            verify(service).executeWebhook(webhookUrl, embeds.take(10))
            assertTrue(runnerIsRunning)

            processQueue!!.invoke()
            verify(service).executeWebhook(webhookUrl, embeds.drop(10))
            verify(runner).stop()
            assertFalse(runnerIsRunning)
        }

    @Test
    fun `send should not schedule work when webhook is not configured`() {
        val runner = mock<PeriodicRunner>()
        val sender = DiscordSend(
            localConfig(""),
            mock<DiscordHttpService>(),
            ErrorTracker(mock<SentryProvider>()),
            runner,
        )

        sender.send(DiscordEmbed(title = "ignored"))

        verify(runner, never()).start(any())
        verify(runner, times(0)).stop()
    }

    private fun localConfig(webhookUrl: String): LocalConfig {
        val values = LocalConfigKeyValues.default().copy(
            discord = LocalConfigKeyValues.Discord(webhookUrl),
        )
        return LocalConfig(
            file = File("unused.json"),
            fileStorage = object : LocalConfigBacking<LocalConfigKeyValues> {
                override fun readSync(file: File) = values
            },
        )
    }
}
