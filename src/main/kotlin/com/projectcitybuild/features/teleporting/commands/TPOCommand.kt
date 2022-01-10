package com.projectcitybuild.features.teleporting.commands

import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.platforms.bungeecord.MessageToSpigot
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.send
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer

class TPOCommand(
    private val proxyServer: ProxyServer
): BungeecordCommand {

    override val label: String = "tpo"
    override val permission = "pcbridge.tp.override"
    override val usageHelp = "/tpo <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.size != 1) {
            input.sender.send().invalidCommandInput(this)
            return
        }

        val targetPlayerName = input.args.first()
        val targetPlayer = proxyServer.players.firstOrNull { it.name.lowercase() == targetPlayerName.lowercase() }
        if (targetPlayer == null) {
            input.sender.send().error("Player $targetPlayerName not found")
            return
        }

        val targetServer = targetPlayer.server

        val isTargetPlayerOnSameServer = input.player.server.info.name == targetPlayer.server.info.name
        val subChannel =
            if (isTargetPlayerOnSameServer) SubChannel.TP_IMMEDIATELY
            else SubChannel.TP_AWAIT_JOIN

        MessageToSpigot(
            targetServer.info,
            subChannel,
            arrayOf(
                input.player.uniqueId.toString(),
                targetPlayer.uniqueId.toString()
            )
        ).send()

        if (!isTargetPlayerOnSameServer) {
            input.player.connect(targetServer.info)
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            else -> null
        }
    }
}
