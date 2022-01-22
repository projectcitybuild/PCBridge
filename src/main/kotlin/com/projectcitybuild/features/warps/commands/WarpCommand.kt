package com.projectcitybuild.features.warps.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.features.warps.repositories.QueuedWarpRepository
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.MessageToSpigot
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.event.ServerConnectEvent
import javax.inject.Inject

class WarpCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val warpRepository: WarpRepository,
    private val queuedWarpRepository: QueuedWarpRepository,
    private val nameGuesser: NameGuesser,
    private val logger: PlatformLogger,
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

        val availableWarps = warpRepository.all()
        val availableWarpNames = availableWarps.map { it.name }

        val targetWarpName = input.args.first()
        val warpName = nameGuesser.guessClosest(targetWarpName, availableWarpNames)
        if (warpName == null) {
            input.sender.send().error("Warp $targetWarpName does not exist")
            return
        }

        val warp = availableWarps.first { it.name == warpName }

        val targetServer = proxyServer.servers[warp.location.serverName]
        if (targetServer == null) {
            logger.warning("Attempted to warp to missing ${warp.location.serverName} server")
            input.sender.send().error("The target server [${warp.location.serverName}] is either offline or invalid")
            return
        }

        val isWarpOnSameServer = input.player.server.info.name == warp.location.serverName
        if (isWarpOnSameServer) {
            MessageToSpigot(
                targetServer,
                SubChannel.WARP_SAME_SERVER,
                arrayOf(
                    warpName,
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
            MessageToSpigot(
                input.player.server.info,
                SubChannel.WARP_ACROSS_SERVER,
                arrayOf(
                    input.player.uniqueId.toString(),
                    targetServer.name,
                )
            ).send()
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> warpRepository.all().map { it.name }
            args.size == 1 -> warpRepository.all().map { it.name }.filter { it.lowercase().startsWith(args.first()) }
            else -> null
        }
    }
}
