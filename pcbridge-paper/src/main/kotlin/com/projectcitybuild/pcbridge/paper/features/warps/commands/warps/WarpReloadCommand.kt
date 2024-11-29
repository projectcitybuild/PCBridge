package com.projectcitybuild.pcbridge.paper.features.warps.commands.warps

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceCommand
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.Plugin

class WarpReloadCommand(
    private val plugin: Plugin,
    private val warpRepository: WarpRepository,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("reload")
            .requiresPermission(PermissionNode.WARP_MANAGE)
            .executesSuspending(plugin, ::execute)
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = traceCommand(context) {
        warpRepository.reload()

        context.source.sender.sendMessage(
            MiniMessage.miniMessage().deserialize("<green>Warps reloaded</green>")
        )
    }
}
