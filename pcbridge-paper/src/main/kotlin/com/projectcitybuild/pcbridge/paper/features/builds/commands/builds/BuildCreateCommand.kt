package com.projectcitybuild.pcbridge.paper.features.builds.commands.builds

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.architecture.commands.scopedSuspending
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.Tracer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.core.support.spigot.extensions.broadcastRich
import com.projectcitybuild.pcbridge.paper.features.builds.buildsTracer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class BuildCreateCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
): BrigadierCommand {
    override fun buildLiteral(): PaperCommandNode {
        return Commands.literal("create")
            .requiresPermission(PermissionNode.BUILDS_MANAGE)
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.scopedSuspending(buildsTracer) {
        val player = context.source.requirePlayer()
        val name = context.getArgument("name", String::class.java)

        val location = player.location
        val build = buildRepository.create(
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