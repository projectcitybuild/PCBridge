package com.projectcitybuild.features.chat.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.features.chat.repositories.ChatIgnoreRepository
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.modules.sessioncache.BungeecordSessionCache
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.extensions.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import javax.inject.Inject

class WhisperCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val playerConfigRepository: PlayerConfigRepository,
    private val chatIgnoreRepository: ChatIgnoreRepository,
    private val sessionCache: BungeecordSessionCache,
    private val nameGuesser: NameGuesser
): BungeecordCommand {

    override val label = "whisper"
    override val aliases = arrayOf("msg")
    override val permission = "pcbridge.chat.whisper"
    override val usageHelp = "/whisper <name> <message>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.isEmpty()) {
            throw InvalidCommandArgumentsException()
        }
        val message = input.args.joinWithWhitespaces(1 until input.args.size)
            ?: throw InvalidCommandArgumentsException()

        val targetPlayerName = input.args.first()

        val targetPlayer = nameGuesser.guessClosest(targetPlayerName, proxyServer.players) { it.name }
        if (targetPlayer == null) {
            input.sender.send().error("Player not found")
            return
        }

        if (targetPlayer == input.player) {
            input.sender.send().error("You cannot directly message yourself")
            return
        }

        if (input.player != null) {
            val playerConfig = playerConfigRepository.get(input.player.uniqueId)
            val targetPlayerConfig = playerConfigRepository.get(targetPlayer.uniqueId)

            if (chatIgnoreRepository.isIgnored(targetPlayerConfig!!.id, playerConfig!!.id)) {
                input.sender.send().error("Cannot send. You are being ignored by $targetPlayerName")
                return
            }
        }

        val senderName = input.player?.displayName ?: "CONSOLE"

        val tc = TextComponent()
            .add("✉ [$senderName -> ${targetPlayer.name}] ") {
                it.color = ChatColor.GRAY
                it.isItalic = true
            }
            .add(TextComponent.fromLegacyText(message).also { charTC ->
                charTC.forEach {
                    it.color = ChatColor.GRAY
                    it.isItalic = true
                }
            })

        targetPlayer.sendMessage(tc)
        input.sender.sendMessage(tc)

        if (input.player != null) {
            sessionCache.lastWhispered[targetPlayer.uniqueId] = input.player.uniqueId
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            args.size == 1 -> proxyServer.players.map { it.name }.filter { it.lowercase().startsWith(args.first().lowercase()) }
            else -> null
        }
    }
}