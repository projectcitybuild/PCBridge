package com.projectcitybuild.pcbridge.paper.features.builds.hooks.commands

import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.paper.features.builds.domain.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.support.brigadier.PaperCommandContext

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