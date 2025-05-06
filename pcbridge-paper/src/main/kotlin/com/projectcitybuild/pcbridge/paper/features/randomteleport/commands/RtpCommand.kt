package com.projectcitybuild.pcbridge.paper.features.randomteleport.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.cooldowns.Cooldown
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.randomteleport.actions.FindRandomLocation
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import kotlin.time.Duration.Companion.seconds

class RtpCommand(
    private val plugin: Plugin,
    private val cooldown: Cooldown,
    private val findRandomLocation: FindRandomLocation,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("rtp")
            .requiresPermission(PermissionNode.TELEPORT_RANDOM)
            .executesSuspending(plugin, ::execute)
            .build()
    }

    suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val miniMessage = MiniMessage.miniMessage()
        val executor = context.source.executor
        val player = executor as? Player
        checkNotNull(player) { "Only players can use this command" }

        cooldown.throttle(5.seconds, player, "rtp")

        executor.sendMessage(
            miniMessage.deserialize("<gray><italic>Searching for a safe location...</italic></gray>")
        )

        val location = findRandomLocation.teleport(player, attempts = 5)
        if (location == null) {
            executor.sendMessage(
                miniMessage.deserialize("<red>Failed to find a safe location</red>")
            )
            return@traceSuspending
        }
        executor.sendMessage(
            miniMessage.deserialize("<green>⚡ Teleported to </green><gray>x=${location.x.toInt()}, y=${location.y.toInt()}, z=${location.z.toInt()}</gray>")
        )
    }
}