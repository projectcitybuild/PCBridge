package com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.homes.domain.repositories.HomeRepository
import com.projectcitybuild.pcbridge.features.homes.homesTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import org.bukkit.plugin.Plugin

class HomeCreateCommand(
    private val plugin: Plugin,
    private val homeRepository: HomeRepository,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override fun literal(): PaperCommandNode = command("create") {
        argument("name", StringArgumentType.greedyString()) {
            executesSuspending(taskExecutor, ::execute)
        }
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(homesTracer) {
        val player = context.source.requirePlayer()
        val name = context.getArgument("name", String::class.java)

        val home =
            homeRepository.create(
                name = name,
                player = player,
                location = player.location,
            )
        context.source.sender.sendMessage(
            l10n.homes.homeCreated(home.name),
        )
    }
}
