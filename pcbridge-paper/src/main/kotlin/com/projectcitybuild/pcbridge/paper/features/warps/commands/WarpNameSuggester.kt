package com.projectcitybuild.pcbridge.paper.features.warps.commands

import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandContext
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository

class WarpNameSuggester(
    private val warpRepository: WarpRepository,
) {
    suspend fun suggest(
        context: CommandContext,
        suggestions: SuggestionsBuilder,
    ) {
        val input = suggestions.remaining.lowercase()

        warpRepository.names()
            .filter { it.name.startsWith(input) }
            .map { it.name }
            .forEach(suggestions::suggest)
    }
}