package com.projectcitybuild.pcbridge.features.worlds.hooks.commands

import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.features.worlds.domain.services.WorldLifecycleService

class WorldNameSuggester(
    private val lifecycleService: WorldLifecycleService,
) {
    fun suggestLoadable(suggestions: SuggestionsBuilder) {
        val input = suggestions.remaining

        lifecycleService
            .list()
            .asSequence()
            .filterNot { it.isLoaded }
            .map { it.definition.name }
            .filter { it.startsWith(input, ignoreCase = true) }
            .forEach(suggestions::suggest)
    }
}
