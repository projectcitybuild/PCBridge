package com.projectcitybuild.features.chat

import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.platforms.bungeecord.extensions.add
import com.projectcitybuild.repositories.ChatIgnoreRepository
import com.projectcitybuild.repositories.LastWhisperedRepository
import com.projectcitybuild.repositories.PlayerConfigRepository
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import java.util.UUID
import javax.inject.Inject

class Whisperer @Inject constructor(
    private val playerConfigRepository: PlayerConfigRepository,
    private val chatIgnoreRepository: ChatIgnoreRepository,
    private val lastWhisperedRepository: LastWhisperedRepository,
    private val nameGuesser: NameGuesser
) {
    class PlayerNotOnlineException : Exception()
    class CannotWhisperSelfException : Exception()
    class BeingIgnoredException(val targetPlayerName: String) : Exception()

    /**
     * Whispers to a player matching (or resembling) a given name
     */
    fun execute(
        whisperingPlayer: Player,
        targetPlayerName: String,
        onlinePlayers: List<Player>,
        message: String,
    ) {
        val targetPlayer = nameGuesser.guessClosest(targetPlayerName, onlinePlayers) { it.name }
            ?: throw PlayerNotOnlineException()

        whisper(
            whisperingPlayer = whisperingPlayer,
            targetPlayer = targetPlayer,
            message = message
        )
    }

    /**
     * Whispers to a player matching a given UUID
     */
    fun execute(
        whisperingPlayer: Player,
        targetPlayerUUID: UUID,
        onlinePlayers: List<Player>,
        message: String,
    ) {
        val targetPlayer = onlinePlayers.firstOrNull { it.uniqueId == targetPlayerUUID }
            ?: throw PlayerNotOnlineException()

        whisper(
            whisperingPlayer = whisperingPlayer,
            targetPlayer = targetPlayer,
            message = message
        )
    }

    private fun whisper(
        whisperingPlayer: Player,
        targetPlayer: Player,
        message: String,
    ) {
        if (targetPlayer == whisperingPlayer) {
            throw CannotWhisperSelfException()
        }

        val playerConfig = playerConfigRepository.get(whisperingPlayer.uniqueId)
        val targetPlayerConfig = playerConfigRepository.get(targetPlayer.uniqueId)

        if (chatIgnoreRepository.isIgnored(targetPlayerConfig!!.id, playerConfig!!.id)) {
            throw BeingIgnoredException(targetPlayerName = targetPlayer.name)
        }

        val tc = TextComponent()
            .add("✉ [${whisperingPlayer.displayName} -> ${targetPlayer.name}] ") {
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
        whisperingPlayer.spigot().sendMessage(tc)

        lastWhisperedRepository.set(
            whisperer = whisperingPlayer.uniqueId,
            targetOfWhisper = targetPlayer.uniqueId,
        )
    }
}
