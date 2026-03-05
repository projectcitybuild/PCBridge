package com.projectcitybuild.pcbridge.paper.features.pim.domain.repositories

import com.projectcitybuild.pcbridge.http.pcb.services.OpElevateHttpService
import net.kyori.adventure.sound.SoundStop.source
import org.bukkit.Server

class OpAuditRepository(
    private val server: Server,
    private val opElevateHttpService: OpElevateHttpService,
) {
    suspend fun auditCommand(command: String, actor: Actor) {
        opElevateHttpService.audit(
            command = command,
            actor = actor.name,
            ip = server.ip(),
            meta = actor.meta,
        )
    }

    sealed class Actor(val name: String, val meta: String? = null) {
        object Rcon: Actor(
            name = "rcon",
        )
        object Console: Actor(
            name = "console",
        )
        data class CommandBlock(val blockMeta: String?): Actor(
            name = "command_block",
            meta = blockMeta,
        )
        object Unknown: Actor(
            name = "unknown",
        )
    }
}

private fun Server.ip(): String {
    if (ip.isEmpty()) return "127.0.0.1"
    return ip
}