package com.projectcitybuild.features.teleporting.commands

import com.projectcitybuild.old_modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.modules.textcomponentbuilder.send
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer

class TPToggleCommand(
    private val proxyServer: ProxyServer,
    private val playerConfigRepository: PlayerConfigRepository
): BungeecordCommand {

    override val label = "tptoggle"
    override val permission = "pcbridge.tp.toggle"
    override val usageHelp = "/tptoggle <off|on>"

    override suspend fun execute(input: BungeecordCommandInput) {
        when {
            input.player == null -> {
                input.sender.send().error("Console cannot use this command")
                return
            }
            input.args.size != 1
                    || input.args.first().lowercase() != "off"
                    || input.args.first().lowercase() != "on" -> {
                input.sender.send().invalidCommandInput(this)
                return
            }
        }

        val toggleOn = input.args.first()

        val targetPlayerName = input.args.first()
        val targetPlayer = proxyServer.players
            .firstOrNull { it.name.lowercase() == targetPlayerName.lowercase() }

        if (targetPlayer == null) {
            input.sender.send().error("Player $targetPlayerName not found")
            return
        }

        val player = playerConfigRepository.get(targetPlayer.uniqueId).also {
            it.isMuted = true
        }
        playerConfigRepository.save(player)

        input.sender.send().success("${targetPlayer.name} has been muted")
        targetPlayer.send().info("You have been muted by ${input.sender.name}")
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            else -> null
        }
    }
}