package com.projectcitybuild.features.chat.commands

import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.old_modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.modules.textcomponentbuilder.send
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent

class WhisperCommand(
    private val proxyServer: ProxyServer,
    private val playerConfigRepository: PlayerConfigRepository
): BungeecordCommand {

    override val label = "whisper"
    override val aliases = arrayOf("msg")
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

        if (input.player != null) {
            val targetPlayerConfig = playerConfigRepository.get(input.player.uniqueId)
            if (targetPlayerConfig.unwrappedChatIgnoreList.contains(input.player.uniqueId)) {
                input.sender.send().error("Cannot send. You are being ignored by $targetPlayerName")
                return
            }
        }

        val message = input.args.joinWithWhitespaces(1 until input.args.size)


        val tc = TextComponent("(DM) ${input.player?.displayName ?: "CONSOLE"} > $message").also {
            it.color = ChatColor.GRAY
            it.isItalic = true
        }
        targetPlayer.sendMessage(tc)
        input.sender.sendMessage(tc)
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            else -> null
        }
    }
}