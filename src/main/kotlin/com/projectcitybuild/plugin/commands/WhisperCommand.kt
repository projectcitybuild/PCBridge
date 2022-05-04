package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.extensions.add
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import com.projectcitybuild.repositories.ChatIgnoreRepository
import com.projectcitybuild.repositories.LastWhisperedRepository
import com.projectcitybuild.repositories.PlayerConfigRepository
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.command.CommandSender
import javax.inject.Inject

class WhisperCommand @Inject constructor(
    private val server: Server,
    private val playerConfigRepository: PlayerConfigRepository,
    private val chatIgnoreRepository: ChatIgnoreRepository,
    private val lastWhisperedRepository: LastWhisperedRepository,
    private val nameGuesser: NameGuesser
) : SpigotCommand {

    override val label = "whisper"
    override val aliases = arrayOf("msg", "m", "w", "t", "tell", "pm")
    override val permission = "pcbridge.chat.whisper"
    override val usageHelp = "/whisper <name> <message>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.isConsole) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.isEmpty()) {
            throw InvalidCommandArgumentsException()
        }
        val message = input.args.joinWithWhitespaces(1 until input.args.size)
            ?: throw InvalidCommandArgumentsException()

        val targetPlayerName = input.args.first()

        val targetPlayer = nameGuesser.guessClosest(targetPlayerName, server.onlinePlayers) { it.name }
        if (targetPlayer == null) {
            input.sender.send().error("Player not found")
            return
        }

        if (targetPlayer == input.player) {
            input.sender.send().error("You cannot directly message yourself")
            return
        }

        val playerConfig = playerConfigRepository.get(input.player.uniqueId)
        val targetPlayerConfig = playerConfigRepository.get(targetPlayer.uniqueId)

        if (chatIgnoreRepository.isIgnored(targetPlayerConfig!!.id, playerConfig!!.id)) {
            input.sender.send().error("Cannot send. You are being ignored by $targetPlayerName")
            return
        }

        val senderName = input.player.displayName

        val tc = TextComponent()
            .add("✉ [$senderName -> ${targetPlayer.name}] ") {
                it.color = ChatColor.GRAY
                it.isItalic = true
            }
            .add(
                TextComponent.fromLegacyText(message).also { charTC ->
                    charTC.forEach {
                        it.color = ChatColor.GRAY
                        it.isItalic = true
                    }
                }
            )

        targetPlayer.spigot().sendMessage(tc)
        input.sender.spigot().sendMessage(tc)

        lastWhisperedRepository.set(
            whisperer = input.player.uniqueId,
            targetOfWhisper = targetPlayer.uniqueId,
        )
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> server.onlinePlayers.map { it.name }
            args.size == 1 -> server.onlinePlayers.map { it.name }.filter { it.lowercase().startsWith(args.first().lowercase()) }
            else -> null
        }
    }
}
