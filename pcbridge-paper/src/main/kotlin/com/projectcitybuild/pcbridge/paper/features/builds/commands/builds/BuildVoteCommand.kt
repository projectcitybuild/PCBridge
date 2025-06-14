package com.projectcitybuild.pcbridge.paper.features.builds.commands.builds

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.core.support.spigot.extensions.broadcastRich
import com.projectcitybuild.pcbridge.paper.features.builds.commands.BuildNameSuggester
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class BuildVoteCommand(
    private val plugin: Plugin,
    private val buildNameSuggester: BuildNameSuggester,
    private val buildRepository: BuildRepository,
): BrigadierCommand {
    override fun buildLiteral(): CommandNode {
        return Commands.literal("vote")
            .requiresPermission(PermissionNode.BUILDS_VOTE)
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .suggestsSuspending(plugin, buildNameSuggester::suggest)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext) = context.traceSuspending {
        val player = context.source.requirePlayer()
        val name = context.getArgument("name", String::class.java)

        val build = buildRepository.vote(name = name, player = player)

        context.source.sender.sendRichMessage(
            "<green>You voted for ${build.name}</green>"
        )
        plugin.server.broadcastRich(
            "<gray>[<red>❤</red>] ${player.name} voted for build \"<white><click:run_command:'/build ${build.name}'><hover:show_text:'Click to teleport'>${build.name}</hover></click></white>\"</gray>",
        )
    }
}