package com.projectcitybuild.pcbridge.paper.features.spawns.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.spawns.repositories.SpawnRepository
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class SetSpawnCommand(
    private val plugin: Plugin,
    private val spawnRepository: SpawnRepository,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("setspawn")
            .requiresPermission(PermissionNode.SPAWN_MANAGE)
            .executesSuspending(plugin, ::execute)
            .build()
    }

    suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val miniMessage = MiniMessage.miniMessage()
        val executor = context.source.executor
        val player = executor as? Player
        checkNotNull(player) { "Only players can use this command" }

        val location = player.location
        spawnRepository.set(location)

        executor.sendMessage(
            miniMessage.deserialize("<green>Set the world spawn point to <gray>${location.x} ${location.y} ${location.z} ${location.pitch} ${location.yaw}</gray></green>")
        )
    }
}