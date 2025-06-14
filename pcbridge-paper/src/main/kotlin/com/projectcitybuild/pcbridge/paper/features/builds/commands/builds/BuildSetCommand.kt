package com.projectcitybuild.pcbridge.paper.features.builds.commands.builds

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.EnumArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.builds.data.EditableBuildField
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class BuildSetCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
): BrigadierCommand {
    override fun buildLiteral(): CommandNode {
        return Commands.literal("set")
            .requiresPermission(PermissionNode.BUILDS_MANAGE)
            .then(
                Commands.argument("id", IntegerArgumentType.integer())
                    .then(
                        Commands.argument("field", EnumArgument(EditableBuildField::class.java))
                            .then(
                                Commands.argument("value", StringArgumentType.greedyString())
                                    .executesSuspending(plugin, ::execute)
                            )
                            .executesSuspending(plugin, ::execute)
                    )
            )
            .build()
    }

    private suspend fun execute(context: CommandContext) = context.traceSuspending {
        val player = context.source.requirePlayer()
        val field = context.getArgument("field", EditableBuildField::class.java)
        val id = context.getArgument("id", Int::class.java)
        val value = context.getOptionalArgument("value", String::class.java) ?: ""

        buildRepository.set(
            id = id,
            player = player,
            field = field,
            value = value,
        )
        context.source.sender.sendRichMessage(
            "<green>Build updated</green>",
        )
    }
}