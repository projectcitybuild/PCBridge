package com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.builds.buildsTracer
import com.projectcitybuild.pcbridge.features.builds.domain.repositories.BuildRepository
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.extensions.broadcastRich
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class BuildCreateCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("create")
            .requiresPermission(PermissionNode.BUILDS_MANAGE)
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .executesSuspending(taskExecutor, ::execute),
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) =
        context.scoped(buildsTracer) {
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

            context.source.sender.sendRichMessage(
                "<green>${build.name} created</green>",
            )
            plugin.server.broadcastRich(
                "<gray>[<red>✎</red>] ${player.name} created build \"<white><click:run_command:'/build ${build.name}'><hover:show_text:'Click to teleport'>${build.name}</hover></click></white>\"</gray>",
            )
        }
}
