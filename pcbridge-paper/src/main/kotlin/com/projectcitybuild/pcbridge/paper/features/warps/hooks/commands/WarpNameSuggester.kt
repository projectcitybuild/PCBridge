package com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands

import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.features.warps.domain.repositories.WarpRepository

class WarpNameSuggester(
    private val warpRepository: WarpRepository,
) {
    suspend fun suggest(
        context: PaperCommandContext,
        suggestions: SuggestionsBuilder,
    ) {
        val input = suggestions.remaining.lowercase()

        warpRepository.names()
            .filter { it.name.startsWith(input) }
            .map { it.name }
            .forEach(suggestions::suggest)
    }
}