package com.projectcitybuild.features.afk.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.features.afk.repositories.AFKRepository
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.extensions.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent

class AFKCommand(
    private val proxyServer: ProxyServer,
    private val afkRepository: AFKRepository,
): BungeecordCommand {

    override val label = "afk"
    override val permission = "pcbridge.afk"
    override val usageHelp = "/afk"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.isNotEmpty()) {
            throw InvalidCommandArgumentsException()
        }
        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }

        if (afkRepository.isAFK(input.player.uniqueId)) {
            afkRepository.remove(input.player.uniqueId)
            proxyServer.broadcast(
                TextComponent()
                    .add("<- ") { it.color = ChatColor.RED }
                    .add("${input.player.name} is no longer AFK").also {
                        it.color = ChatColor.GRAY
                        it.isItalic = true
                    }
            )
        } else {
            afkRepository.add(input.player.uniqueId)
            proxyServer.broadcast(
                TextComponent()
                    .add("<- ") { it.color = ChatColor.GREEN }
                    .add("${input.player.name} is now AFK").also {
                        it.color = ChatColor.GRAY
                        it.isItalic = true
                    }
            )
        }
    }
}
