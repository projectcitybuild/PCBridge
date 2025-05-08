package com.projectcitybuild.pcbridge.paper.features.borders.commands.border

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.borders.repositories.WorldBorderRepository
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class BorderDeleteCommand(
    private val plugin: Plugin,
    private val worldBorderRepository: WorldBorderRepository,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("delete")
            .requiresPermission(PermissionNode.BORDER_MANAGE)
            .executesSuspending(plugin, ::execute)
            .build()
    }

    suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val player = context.source.sender as? Player
        checkNotNull(player) { "Only players can use this command" }

        val world = player.location.world
        val border = worldBorderRepository.get(world)
        if (border == null) {
            player.sendRichMessage("<red>Error: ${world.name} does not currently have a world border")
            return@traceSuspending
        }

        worldBorderRepository.delete(world)

        player.sendRichMessage("<gray>World border deleted for ${world.name}</gray>")
    }
}