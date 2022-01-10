package com.projectcitybuild.features.chat.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.modules.sessioncache.BungeecordSessionCache
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
    private val playerConfigRepository: PlayerConfigRepository,
    private val sessionCache: BungeecordSessionCache
): BungeecordCommand {

    override val label = "whisper"
    override val aliases = arrayOf("msg")
    override val permission = "pcbridge.chat.whisper"
    override val usageHelp = "/whisper <name> <message>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.isEmpty()) {
            throw InvalidCommandArgumentsException()
        }
        val targetPlayerName = input.args.first()

        val targetPlayer = proxyServer.players
            .firstOrNull { it.name.lowercase() == targetPlayerName.lowercase() }

        if (targetPlayer == null) {
            input.sender.send().error("Player not found")
            return
        }

        if (targetPlayer == input.player) {
            input.sender.send().error("You cannot directly message yourself")
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
        val senderName = input.player?.displayName ?: "CONSOLE"

        val tc = TextComponent(" [$senderName -> ${targetPlayer.name}] $message").also {
            it.color = ChatColor.GRAY
            it.isItalic = true
        }
        targetPlayer.sendMessage(tc)
        input.sender.sendMessage(tc)

        if (input.player != null) {
            sessionCache.lastWhispered[targetPlayer.uniqueId] = input.player.uniqueId
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            else -> null
        }
    }
}