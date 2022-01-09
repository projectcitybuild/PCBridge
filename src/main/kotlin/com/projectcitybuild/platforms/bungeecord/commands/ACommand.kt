package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.extensions.add
import com.projectcitybuild.platforms.bungeecord.send
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent

class ACommand(
    private val proxyServer: ProxyServer
): BungeecordCommand {

    override val label: String = "a"
    override val permission = "pcbridge.chat.staff_channel"
    override val usageHelp = "/a <message>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.isEmpty()) {
            input.sender.send().invalidCommandInput(this)
            return
        }

        val message = input.args.joinToString()

        proxyServer.players.forEach { player ->
            if (player.hasPermission("pcbridge.chat.staff_channel"))
                player.sendMessage(
                    TextComponent()
                        .add("(Staff) ${player.displayName}") { it.color = ChatColor.YELLOW }
                        .add(" » ") { it.color = ChatColor.GRAY }
                        .add(message) {
                            it.color = ChatColor.YELLOW
                            it.isItalic = true
                        }
                )
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return null
    }
}
