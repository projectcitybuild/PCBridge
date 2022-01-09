package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.send
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent

class WhisperCommand(
    private val proxyServer: ProxyServer,
): BungeecordCommand {

    override val label = "whisper"
    override val permission = "pcbridge.chat.whisper"
    override val usageHelp = "/whisper <name> <message>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.isEmpty()) {
            input.sender.send().invalidCommandInput(this)
            return
        }
        val targetPlayerName = input.args.first()

        val targetPlayer = proxyServer.players
            .firstOrNull { it.name.lowercase() == targetPlayerName.lowercase() }

        if (targetPlayer == null) {
            input.sender.send().error("Player not found")
            return
        }

        // TODO: check if on ignore list

        val message = input.args.joinWithWhitespaces(1 until input.args.size)
        val tc = TextComponent("(DM) ${input.player?.displayName ?: "CONSOLE"} > $message").also {
            it.color = ChatColor.GRAY
            it.isItalic = true
        }
        targetPlayer.sendMessage(tc)
        input.sender.sendMessage(tc)

        // TODO: send message as [sender->receiver] message
    }
}