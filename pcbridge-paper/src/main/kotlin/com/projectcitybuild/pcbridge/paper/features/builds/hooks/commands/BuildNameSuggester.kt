package com.projectcitybuild.pcbridge.paper.features.builds.hooks.commands

import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.features.builds.domain.repositories.BuildRepository

class BuildNameSuggester(
    private val buildRepository: BuildRepository,
) {
    suspend fun suggest(
        context: PaperCommandContext,
        suggestions: SuggestionsBuilder,
    ) {
        val name = suggestions.remaining.lowercase()

        buildRepository.names()
            .filter { it.lowercase().startsWith(name) }
            .forEach(suggestions::suggest)
    }
}