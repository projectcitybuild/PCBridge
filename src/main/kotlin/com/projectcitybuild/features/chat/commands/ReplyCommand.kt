package com.projectcitybuild.features.chat.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import com.projectcitybuild.repositories.ChatIgnoreRepository
import com.projectcitybuild.repositories.LastWhisperedRepository
import com.projectcitybuild.repositories.PlayerConfigRepository
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import javax.inject.Inject

class ReplyCommand @Inject constructor(
    private val server: Server,
    private val playerConfigRepository: PlayerConfigRepository,
    private val chatIgnoreRepository: ChatIgnoreRepository,
    private val lastWhisperedRepository: LastWhisperedRepository,
) : SpigotCommand {

    override val label = "reply"
    override val aliases = arrayOf("r")
    override val permission = "pcbridge.chat.whisper"
    override val usageHelp = "/reply <message>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.isConsole) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.isEmpty()) {
            throw InvalidCommandArgumentsException()
        }

        val playerWhoLastWhispered = lastWhisperedRepository.getLastWhisperer(input.player.uniqueId)
        if (playerWhoLastWhispered == null) {
            input.sender.send().error("You have not been direct messaged by anyone yet")
            return
        }

        val targetPlayer = server.getPlayer(playerWhoLastWhispered)
        if (targetPlayer == null) {
            lastWhisperedRepository.remove(input.player.uniqueId)
            input.sender.send().error("Player not online")
            return
        }

        val playerConfig = playerConfigRepository.get(input.player.uniqueId)
        val targetPlayerConfig = playerConfigRepository.get(targetPlayer.uniqueId)
        if (chatIgnoreRepository.isIgnored(targetPlayerConfig!!.id, playerConfig!!.id)) {
            input.sender.send().error("Cannot send. You are being ignored by ${targetPlayer.name}")
            return
        }

        val message = input.args.joinToString(separator = " ")
        val senderName = input.player.displayName

        val tc = TextComponent("✉ [$senderName -> ${targetPlayer.name}] $message").also {
            it.color = ChatColor.GRAY
            it.isItalic = true
        }
        targetPlayer.spigot().sendMessage(tc)
        input.sender.spigot().sendMessage(tc)

        lastWhisperedRepository.set(
            whisperer = input.player.uniqueId,
            targetOfWhisper = targetPlayer.uniqueId,
        )
    }
}
