package com.projectcitybuild.features.afk.commands

import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.send
import com.projectcitybuild.old_modules.sessioncache.SessionCache
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer

class AFKCommand(
    private val proxyServer: ProxyServer,
    private val sessionCache: SessionCache
): BungeecordCommand {

    override val label = "afk"
    override val permission = "pcbridge.afk"
    override val usageHelp = "/afk"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.isNotEmpty()) {
            input.sender.send().invalidCommandInput(this)
            return
        }
        if (input.isConsoleSender) {
            input.sender.send().error("Console cannot use this command")
            return
        }

        val player = input.sender as ProxiedPlayer
        if (sessionCache.afkPlayerList.contains(player.uniqueId)) {
            sessionCache.afkPlayerList.remove(player.uniqueId)
            proxyServer.broadcast(
                TextComponent("${player.displayName} is no longer AFK").also {
                    it.color = ChatColor.GRAY
                    it.isItalic = true
                }
            )
        } else {
            sessionCache.afkPlayerList.add(player.uniqueId)
            proxyServer.broadcast(
                TextComponent("${player.displayName} is now AFK").also {
                    it.color = ChatColor.GRAY
                    it.isItalic = true
                }
            )
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return null
    }
}
