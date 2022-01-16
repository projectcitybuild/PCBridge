package com.projectcitybuild.features.hub.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.old_modules.storage.HubFileStorage
import com.projectcitybuild.platforms.bungeecord.MessageToSpigot
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.modules.textcomponentbuilder.send
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import javax.inject.Inject

class HubCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val hubFileStorage: HubFileStorage
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

        val warp = hubFileStorage.load()
        if (warp == null) {
            input.sender.send().error("Hub has not been set")
            return
        }

        val targetServer = proxyServer.servers[warp.serverName]
        if (targetServer == null) {
            input.sender.send().error("The hub is currently offline or unavailable. Please try again later")
            return
        }

        val isWarpOnSameServer = input.player.server.info.name == warp.serverName
        val subChannel =
            if (isWarpOnSameServer) SubChannel.WARP_IMMEDIATELY
            else SubChannel.WARP_AWAIT_JOIN

        MessageToSpigot(
            targetServer,
            subChannel,
            arrayOf(
                input.player.uniqueId.toString(),
                warp.worldName,
                warp.x,
                warp.y,
                warp.z,
                warp.pitch,
                warp.yaw,
            )
        ).send()

        if (!isWarpOnSameServer) {
            input.player.connect(targetServer)
        }
    }
}
