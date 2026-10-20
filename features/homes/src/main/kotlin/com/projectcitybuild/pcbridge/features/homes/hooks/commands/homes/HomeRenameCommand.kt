package com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.homes.domain.repositories.HomeRepository
import com.projectcitybuild.pcbridge.features.homes.homesTracer
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.HomeNameSuggester
import com.projectcitybuild.pcbridge.features.homes.hooks.dialogs.HomeRenameDialog
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import org.bukkit.plugin.Plugin

class HomeRenameCommand(
    private val plugin: Plugin,
    private val homeNameSuggester: HomeNameSuggester,
    private val homeRepository: HomeRepository,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override fun literal() = command("rename") {
        argument("name", StringArgumentType.greedyString()) {
            suggestsSuspending(plugin, homeNameSuggester::suggest)
            executesSuspending(taskExecutor, ::execute)
        }
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(homesTracer) {
        val player = context.source.requirePlayer()
        val name = context.getArgument("name", String::class.java)

        val home = homeRepository.get(player.uniqueId, name)
        checkNotNull(home) { l10n.homes.errorHomeNotFound(name) }

        val dialog =
            HomeRenameDialog.build(
                homeId = home.id,
                prevName = home.name,
                newName = home.name,
            )
        player.showDialog(dialog)
    }
}
