package com.projectcitybuild.pcbridge.paper.features.pim.hooks.listener

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scoped
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SerializableLocation
import com.projectcitybuild.pcbridge.paper.features.pim.domain.repositories.OpAuditRepository
import com.projectcitybuild.pcbridge.paper.features.pim.pimTracer
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.RemoteConsoleCommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerCommandEvent

class OpAuditingListener(
    private val opAuditRepository: OpAuditRepository,
): Listener {
    /**
     * Monitors /op /deop usage by console, command blocks or rcon,
     * and logs their usage for auditing purposes
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    suspend fun onServerCommandEvent(event: ServerCommandEvent) {
        val command = event.command.lowercase().removePrefix("/")
        if (!command.startsWith("op ") && !command.startsWith("deop ")) {
            return
        }
        event.scoped(pimTracer, this::class.java) {
            val args = command.split(" ")
            if (args.size < 2) return@scoped

            opAuditRepository.auditCommand(
                command = command,
                actor = event.sender.actor()
            )
        }
    }
}

private fun CommandSender.actor() = when (this) {
    is RemoteConsoleCommandSender -> OpAuditRepository.Actor.Rcon
    is ConsoleCommandSender -> OpAuditRepository.Actor.Console
    is BlockCommandSender -> OpAuditRepository.Actor.CommandBlock(
        blockMeta = gson().serializer().toJson(
            SerializableLocation.fromLocation(block.location, block.world)
        )
    )
    else -> OpAuditRepository.Actor.Unknown
}
