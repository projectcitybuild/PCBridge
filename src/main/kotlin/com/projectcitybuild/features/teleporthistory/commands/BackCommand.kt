package com.projectcitybuild.features.teleporthistory.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.features.teleporthistory.repositories.LastKnownLocationRepositoy
import com.projectcitybuild.features.warps.repositories.QueuedWarpRepository
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.MessageToSpigot
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.ProxyServer
import java.time.LocalDateTime
import javax.inject.Inject

class BackCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val lastKnownLocationRepositoy: LastKnownLocationRepositoy,
    private val queuedWarpRepository: QueuedWarpRepository,
): BungeecordCommand {

    override val label: String = "back"
    override val permission = "pcbridge.tp.back"
    override val usageHelp = "/back"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.isNotEmpty()) {
            throw InvalidCommandArgumentsException()
        }

        val lastKnownLocation = lastKnownLocationRepositoy.get(input.player.uniqueId)
        if (lastKnownLocation == null) {
            input.sender.send().error("No last known location")
            return
        }

        val isDestinationOnSameServer = input.player.server.info.name == lastKnownLocation.location.serverName
        if (isDestinationOnSameServer) {
            MessageToSpigot(
                input.player.server.info,
                SubChannel.WARP_SAME_SERVER,
                arrayOf(
                    "last known location",
                    input.player.uniqueId.toString(),
                    lastKnownLocation.location.worldName,
                    lastKnownLocation.location.x,
                    lastKnownLocation.location.y,
                    lastKnownLocation.location.z,
                    lastKnownLocation.location.pitch,
                    lastKnownLocation.location.yaw,
                )
            ).send()
        } else {
            val warp = Warp(
                name = "/back",
                location = lastKnownLocation.location,
                createdAt = LocalDateTime.now() // Not needed
            )
            queuedWarpRepository.queue(input.player.uniqueId, warp)
            MessageToSpigot(
                input.player.server.info,
                SubChannel.WARP_ACROSS_SERVER,
                arrayOf(
                    input.player.uniqueId.toString(),
                    lastKnownLocation.location.serverName,
                )
            ).send()
        }
    }
}
