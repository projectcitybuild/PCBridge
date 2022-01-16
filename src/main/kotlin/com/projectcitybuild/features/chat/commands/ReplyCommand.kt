package com.projectcitybuild.features.chat.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.features.chat.repositories.ChatIgnoreRepository
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.modules.sessioncache.BungeecordSessionCache
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.modules.textcomponentbuilder.send
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent

class ReplyCommand(
    private val proxyServer: ProxyServer,
    private val playerConfigRepository: PlayerConfigRepository,
    private val chatIgnoreRepository: ChatIgnoreRepository,
    private val sessionCache: BungeecordSessionCache
): BungeecordCommand {

    override val label = "reply"
    override val aliases = arrayOf("r")
    override val permission = "pcbridge.chat.whisper"
    override val usageHelp = "/reply <message>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.isEmpty()) {
            throw InvalidCommandArgumentsException()
        }

        val playerWhoLastWhispered = sessionCache.lastWhispered[input.player.uniqueId]
        if (playerWhoLastWhispered == null) {
            input.sender.send().error("You have not been direct messaged by anyone yet")
            return
        }

        val targetPlayer = proxyServer.getPlayer(playerWhoLastWhispered)
        if (targetPlayer == null) {
            sessionCache.lastWhispered.remove(input.player.uniqueId)
            input.sender.send().error("Player not online")
            return
        }

        val playerConfig = playerConfigRepository.get(input.player.uniqueId)
        val targetPlayerConfig = playerConfigRepository.get(targetPlayer.uniqueId)
        if (chatIgnoreRepository.isIgnored(playerConfig!!.id, targetPlayerConfig!!.id)) {
            input.sender.send().error("Cannot send. You are being ignored by ${targetPlayer.name}")
            return
        }

        val message = input.args.joinToString(separator = " ")
        val senderName = input.player?.displayName ?: "CONSOLE"

        val tc = TextComponent(" [$senderName -> ${targetPlayer.name}] $message").also {
            it.color = ChatColor.GRAY
            it.isItalic = true
        }
        targetPlayer.sendMessage(tc)
        input.sender.sendMessage(tc)

        sessionCache.lastWhispered[targetPlayer.uniqueId] = input.player.uniqueId
    }
}