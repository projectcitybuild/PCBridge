package com.projectcitybuild.pcbridge.features.worlds.hooks.listeners

import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldDefinition
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldRegistration
import com.projectcitybuild.pcbridge.features.worlds.domain.services.WorldLifecycleService
import org.bukkit.event.server.ServerLoadEvent
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.util.logging.Level
import java.util.logging.Logger

class WorldStartupListenerTest {
    private val plugin = mock(JavaPlugin::class.java)
    private val lifecycleService = mock(WorldLifecycleService::class.java)
    private val event = mock(ServerLoadEvent::class.java)
    private val logger = mock(Logger::class.java)

    @Test
    fun `loads registered worlds that are not already loaded`() {
        whenever(plugin.logger).thenReturn(logger)
        whenever(lifecycleService.list()).thenReturn(
            listOf(
                registration("already-loaded", isLoaded = true),
                registration("needs-loading", isLoaded = false),
            ),
        )
        val listener = WorldStartupListener(plugin, lifecycleService)

        listener.onServerLoad(event)

        verify(lifecycleService).load("needs-loading")
    }

    @Test
    fun `logs registry failures without aborting server load handling`() {
        whenever(plugin.logger).thenReturn(logger)
        whenever(lifecycleService.list()).thenThrow(IllegalArgumentException("invalid registry"))
        val listener = WorldStartupListener(plugin, lifecycleService)

        listener.onServerLoad(event)

        verify(logger).log(
            eq(Level.SEVERE),
            eq("Could not read registered world definitions; skipping startup reconciliation"),
            any<Exception>(),
        )
    }

    private fun registration(
        name: String,
        isLoaded: Boolean,
    ) = WorldRegistration(
        definition = WorldDefinition(
            registryKey = name,
            name = name,
            alias = null,
            environment = null,
            worldType = null,
            seed = null,
            generator = null,
            generatorSettings = null,
            generateStructures = null,
            spawnLocation = null,
            scale = null,
            respawnWorld = null,
            pvp = null,
        ),
        isLoaded = isLoaded,
    )
}
