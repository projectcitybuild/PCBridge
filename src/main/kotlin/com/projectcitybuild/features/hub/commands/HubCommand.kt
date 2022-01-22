package com.projectcitybuild.features.hub.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.features.warps.repositories.QueuedWarpRepository
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.old_modules.storage.HubFileStorage
import com.projectcitybuild.platforms.bungeecord.MessageToSpigot
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.event.ServerConnectEvent
import java.time.LocalDateTime
import javax.inject.Inject

class HubCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val hubFileStorage: HubFileStorage,
    private val queuedWarpRepository: QueuedWarpRepository,
): BungeecordCommand {

    override val label: String = "hub"
    override val permission = "pcbridge.hub.use"
    override val usageHelp = "/hub"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.isNotEmpty()) {
            throw InvalidCommandArgumentsException()
        }
        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }

        val hub = hubFileStorage.load()
        if (hub == null) {
            input.sender.send().error("Hub has not been set")
            return
        }

        val targetServer = proxyServer.servers[hub.serverName]
        if (targetServer == null) {
            input.sender.send().error("The hub is currently offline or unavailable. Please try again later")
            return
        }

        val warp = Warp(
            name = "hub",
            location = CrossServerLocation(
                serverName = hub.serverName,
                worldName = hub.worldName,
                x = hub.x,
                y = hub.y,
                z = hub.z,
                pitch = hub.pitch,
                yaw = hub.yaw,
            ),
            createdAt = LocalDateTime.now(),
        )

        val isHubOnSameServer = input.player.server.info.name == warp.location.serverName
        if (isHubOnSameServer) {
            MessageToSpigot(
                targetServer,
                SubChannel.WARP_SAME_SERVER,
                arrayOf(
                    "hub",
                    input.player.uniqueId.toString(),
                    warp.location.worldName,
                    warp.location.x,
                    warp.location.y,
                    warp.location.z,
                    warp.location.pitch,
                    warp.location.yaw,
                )
            ).send()
        } else {
            queuedWarpRepository.queue(input.player.uniqueId, warp)
            input.player.connect(targetServer, ServerConnectEvent.Reason.COMMAND)
        }
    }
}
