package com.projectcitybuild.pcbridge.core.localconfig

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LocalConfigKeyValuesTest {
    @Test
    fun `default config provides usable local development settings`() {
        val config = LocalConfigKeyValues.default()

        assertEquals(Environment.DEV, config.environment)
        assertEquals("pcbridge_local", config.api.token)
        assertEquals("http://api.localhost/", config.api.baseUrl)
        assertEquals(LocalConfigKeyValues.Api.LogLevel.Trace, config.api.logLevel)
        assertEquals("127.0.0.1", config.webServer.host)
        assertEquals(8080, config.webServer.port)
        assertEquals("REPLACE_WITH_A_RANDOM_SECRET", config.webServer.token)
        assertEquals("", config.observability.sentryDsn)
        assertEquals(1.0, config.observability.traceSampleRate)
        assertEquals("", config.discord.contentAlertWebhook)
        assertEquals(4_096, config.worlds?.borderFillMaxChunks)
    }

    @Test
    fun `environment serializes using its configured names`() {
        assertEquals("\"dev\"", Json.encodeToString(Environment.DEV))
        assertEquals("\"production\"", Json.encodeToString(Environment.PRODUCTION))
        assertFalse(Environment.DEV.isProduction)
        assertTrue(Environment.PRODUCTION.isProduction)
    }

    @Test
    fun `all log levels except None are considered enabled`() {
        assertFalse(LocalConfigKeyValues.Api.LogLevel.None.enabled)
        assertTrue(LocalConfigKeyValues.Api.LogLevel.Trace.enabled)
        assertTrue(LocalConfigKeyValues.Api.LogLevel.Debug.enabled)
        assertTrue(LocalConfigKeyValues.Api.LogLevel.Info.enabled)
    }
}
