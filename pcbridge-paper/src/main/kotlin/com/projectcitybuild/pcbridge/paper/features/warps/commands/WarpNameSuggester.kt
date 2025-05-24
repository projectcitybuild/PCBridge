package com.projectcitybuild.pcbridge.paper.features.warps.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WarpNameSuggester(
    private val warpRepository: WarpRepository,
) {
    suspend fun suggest(
        context: CommandContext<CommandSourceStack>,
        suggestions: SuggestionsBuilder,
    ) = withContext(Dispatchers.IO) {
        val input = suggestions.remaining.lowercase()

        warpRepository.names()
            .filter { it.name.startsWith(input) }
            .map { it.name }
            .forEach(suggestions::suggest)
    }
}