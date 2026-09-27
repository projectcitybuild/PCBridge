package com.projectcitybuild.pcbridge.paper.features.builds.hooks.commands.builds

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.paper.core.libs.permissions.PermissionNode
import com.projectcitybuild.pcbridge.paper.features.builds.buildsTracer
import com.projectcitybuild.pcbridge.paper.features.builds.domain.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.features.builds.hooks.commands.BuildNameSuggester
import com.projectcitybuild.pcbridge.paper.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.runtime.commands.scoped
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class BuildDeleteCommand(
    private val plugin: Plugin,
    private val buildNameSuggester: BuildNameSuggester,
    private val buildRepository: BuildRepository,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("delete")
            .requiresPermission(PermissionNode.BUILDS_MANAGE)
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .suggestsSuspending(plugin, buildNameSuggester::suggest)
                    .executesSuspending(plugin, ::execute),
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) =
        context.scoped(buildsTracer) {
            val player = context.source.requirePlayer()
            val name = context.getArgument("name", String::class.java)

            buildRepository.delete(
                name = name,
                player = player,
            )
            context.source.sender.sendRichMessage("<green>$name deleted</green>")
        }
}
