package com.projectcitybuild.pcbridge.paper.features.builds.commands.builds

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.architecture.commands.scopedSuspending
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.Tracer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.features.builds.buildsTracer
import com.projectcitybuild.pcbridge.paper.features.builds.commands.BuildNameSuggester
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class BuildUnvoteCommand(
    private val plugin: Plugin,
    private val buildNameSuggester: BuildNameSuggester,
    private val buildRepository: BuildRepository,
): BrigadierCommand {
    override fun buildLiteral(): PaperCommandNode {
        return Commands.literal("unvote")
            .requiresPermission(PermissionNode.BUILDS_VOTE)
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .suggestsSuspending(plugin, buildNameSuggester::suggest)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.scopedSuspending(buildsTracer) {
        val player = context.source.requirePlayer()
        val name = context.getArgument("name", String::class.java)

        val build = buildRepository.unvote(name = name, player = player)

        context.source.sender.sendRichMessage(
            "<gray>You removed your vote for ${build.name}</gray>",
        )
    }
}