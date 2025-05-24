package com.projectcitybuild.pcbridge.paper.features.homes.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.paper.features.homes.repositories.HomeRepository
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player

class HomeNameSuggester(
    private val homeRepository: HomeRepository,
) {
    suspend fun suggest(
        context: CommandContext<CommandSourceStack>,
        suggestions: SuggestionsBuilder,
    ) = withContext(Dispatchers.IO) {
        val player = context.source.executor as? Player
            ?: return@withContext

        val input = suggestions.remaining.lowercase()

        homeRepository.names(playerUUID = player.uniqueId)
            .filter { it.name.startsWith(input) }
            .map { it.name }
            .forEach(suggestions::suggest)
    }
}