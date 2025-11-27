package com.projectcitybuild.pcbridge.paper.features.homes.hooks.commands

import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.features.homes.domain.repositories.HomeRepository
import org.bukkit.entity.Player

class HomeNameSuggester(
    private val homeRepository: HomeRepository,
) {
    suspend fun suggest(
        context: PaperCommandContext,
        suggestions: SuggestionsBuilder,
    ) {
        val player = context.source.executor as? Player
            ?: return

        val input = suggestions.remaining

        homeRepository.names(playerUUID = player.uniqueId)
            .filter { it.name.startsWith(input, ignoreCase = true) }
            .map { it.name }
            .forEach(suggestions::suggest)
    }
}