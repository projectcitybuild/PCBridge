package com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.builds.BuildsPermissions
import com.projectcitybuild.pcbridge.features.builds.buildsTracer
import com.projectcitybuild.pcbridge.features.builds.domain.repositories.BuildRepository
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import org.bukkit.plugin.Plugin

class BuildCreateCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val build = command("create") {
        description = "Creates a new build"
        requiresPermission(BuildsPermissions.MANAGE)
        argument("name", StringArgumentType.greedyString()) {
            executesSuspending(taskExecutor, ::execute)
        }
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(buildsTracer) {
        val player = context.source.requirePlayer()
        val name = context.getArgument("name", String::class.java)

        val location = player.location
        val build =
            buildRepository.create(
                name = name,
                player = player,
                world = location.world.name,
                location = player.location,
            )

        context.source.sender.sendMessage(l10n.builds.buildCreated(build.name))
        plugin.server.broadcast(l10n.builds.buildCreatedAnnouncement(player.name, build.name))
    }
}
