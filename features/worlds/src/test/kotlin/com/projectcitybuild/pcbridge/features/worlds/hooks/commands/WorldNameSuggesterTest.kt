package com.projectcitybuild.pcbridge.features.worlds.hooks.commands

import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldDefinition
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldRegistration
import com.projectcitybuild.pcbridge.features.worlds.domain.services.WorldLifecycleService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class WorldNameSuggesterTest {
    private val lifecycleService = mock(WorldLifecycleService::class.java)

    @Test
    fun `suggests matching unloaded registered worlds`() {
        whenever(lifecycleService.list()).thenReturn(
            listOf(
                registration("world", isLoaded = true),
                registration("world_nether", isLoaded = false),
                registration("creative", isLoaded = false),
            ),
        )
        val suggestions = SuggestionsBuilder("WORLD_", 0)

        WorldNameSuggester(lifecycleService).suggestLoadable(suggestions)

        assertEquals(listOf("world_nether"), suggestions.build().list.map { it.text })
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
