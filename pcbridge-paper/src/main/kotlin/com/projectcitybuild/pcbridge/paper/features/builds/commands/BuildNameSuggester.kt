package com.projectcitybuild.pcbridge.paper.features.builds.commands

import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandContext
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository

class BuildNameSuggester(
    private val buildRepository: BuildRepository,
) {
    suspend fun suggest(
        context: CommandContext,
        suggestions: SuggestionsBuilder,
    ) {
        val name = suggestions.remaining.lowercase()

        buildRepository.names()
            .filter { it.lowercase().startsWith(name) }
            .forEach(suggestions::suggest)
    }
}