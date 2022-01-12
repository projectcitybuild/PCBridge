package com.projectcitybuild.features.warps.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.old_modules.storage.WarpFileStorage
import com.projectcitybuild.platforms.bungeecord.MessageToSpigot
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.modules.textcomponentbuilder.send
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer

class WarpCommand(
    private val proxyServer: ProxyServer,
    private val warpFileStorage: WarpFileStorage,
    private val nameGuesser: NameGuesser
): BungeecordCommand {

    override val label: String = "warp"
    override val permission = "pcbridge.warp.use"
    override val usageHelp = "/warp <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }
        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }

        val availableWarpNames = warpFileStorage.keys()

        val targetWarpName = input.args.first()
        val warpName = nameGuesser.guessClosest(targetWarpName, availableWarpNames)
        if (warpName == null) {
            input.sender.send().error("Warp $warpName does not exist")
            return
        }

        val warp = warpFileStorage.load(warpName)!!

        val targetServer = proxyServer.servers[warp.serverName]
        if (targetServer == null) {
            input.sender.send().error("The target server [${warp.serverName}] is either offline or invalid")
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

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> warpFileStorage.keys()
            else -> null
        }
    }
}
