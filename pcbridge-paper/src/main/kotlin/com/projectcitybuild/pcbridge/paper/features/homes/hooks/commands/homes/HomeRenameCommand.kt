package com.projectcitybuild.pcbridge.paper.features.homes.hooks.commands.homes

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.architecture.commands.scopedSuspending
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.Tracer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.features.homes.hooks.commands.HomeNameSuggester
import com.projectcitybuild.pcbridge.paper.features.homes.domain.repositories.HomeRepository
import com.projectcitybuild.pcbridge.paper.features.homes.homesTracer
import com.projectcitybuild.pcbridge.paper.features.homes.hooks.dialogs.HomeRenameDialog
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class HomeRenameCommand(
    private val plugin: Plugin,
    private val homeNameSuggester: HomeNameSuggester,
    private val homeRepository: HomeRepository,
): BrigadierCommand {
    override fun buildLiteral(): PaperCommandNode {
        return Commands.literal("rename")
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .suggestsSuspending(plugin, homeNameSuggester::suggest)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.scopedSuspending(homesTracer) {
        val player = context.source.requirePlayer()
        val name = context.getArgument("name", String::class.java)

        val home = homeRepository.get(player.uniqueId, name)
        checkNotNull(home) { l10n.errorHomeNotFound(name) }

        val dialog = HomeRenameDialog.build(
            homeId = home.id,
            prevName = home.name,
            newName = home.name,
        )
        player.showDialog(dialog)
    }
}