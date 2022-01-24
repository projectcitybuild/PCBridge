package com.projectcitybuild.features.hub.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.features.hub.repositories.HubRepository
import com.projectcitybuild.features.warps.repositories.QueuedWarpRepository
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.MessageToSpigot
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.event.ServerConnectEvent
import javax.inject.Inject

class HubCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val hubRepository: HubRepository,
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

        val hub = hubRepository.get()
        if (hub == null) {
            input.sender.send().error("Hub has not been set")
            return
        }

        val targetServer = proxyServer.servers[hub.location.serverName]
        if (targetServer == null) {
            input.sender.send().error("The hub is currently offline or unavailable. Please try again later")
            return
        }

        val isHubOnSameServer = input.player.server.info.name == hub.location.serverName
        if (isHubOnSameServer) {
            MessageToSpigot(
                targetServer,
                SubChannel.WARP_SAME_SERVER,
                arrayOf(
                    "hub",
                    input.player.uniqueId.toString(),
                    hub.location.worldName,
                    hub.location.x,
                    hub.location.y,
                    hub.location.z,
                    hub.location.pitch,
                    hub.location.yaw,
                )
            ).send()
        } else {
            queuedWarpRepository.queue(input.player.uniqueId, hub)
            input.player.connect(targetServer, ServerConnectEvent.Reason.COMMAND)
        }
    }
}
