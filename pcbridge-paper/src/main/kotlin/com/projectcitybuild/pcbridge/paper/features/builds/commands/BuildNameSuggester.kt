package com.projectcitybuild.pcbridge.paper.features.builds.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BuildNameSuggester(
    private val buildRepository: BuildRepository,
) {
    suspend fun suggest(
        context: CommandContext<CommandSourceStack>,
        suggestions: SuggestionsBuilder,
    ) = withContext(Dispatchers.IO) {
        val name = suggestions.remaining.lowercase()

        buildRepository.names()
            .filter { it.lowercase().startsWith(name) }
            .forEach(suggestions::suggest)
    }
}