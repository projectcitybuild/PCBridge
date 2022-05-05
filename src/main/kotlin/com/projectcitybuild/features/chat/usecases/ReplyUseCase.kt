package com.projectcitybuild.features.chat.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.repositories.ChatIgnoreRepository
import com.projectcitybuild.repositories.LastWhisperedRepository
import com.projectcitybuild.repositories.PlayerConfigRepository
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import javax.inject.Inject

class ReplyUseCase @Inject constructor(
    private val playerConfigRepository: PlayerConfigRepository,
    private val chatIgnoreRepository: ChatIgnoreRepository,
    private val lastWhisperedRepository: LastWhisperedRepository,
) {
    sealed class FailureReason {
        object NO_ONE_TO_REPLY_TO: FailureReason()
        object PLAYER_NOT_ONLINE: FailureReason()
        data class IGNORED(val targetPlayerName: String): FailureReason()
    }

    fun execute(
        player: Player,
        onlinePlayers: List<Player>,
        message: String,
    ): Result<Unit, FailureReason> {
        val playerUUIDWhoLastWhispered = lastWhisperedRepository.getLastWhisperer(player.uniqueId)
            ?: return Failure(FailureReason.NO_ONE_TO_REPLY_TO)

        val targetPlayer = onlinePlayers.firstOrNull { it.uniqueId == playerUUIDWhoLastWhispered }
        if (targetPlayer == null) {
            lastWhisperedRepository.remove(player.uniqueId)
            return Failure(FailureReason.PLAYER_NOT_ONLINE)
        }

        val playerConfig = playerConfigRepository.get(player.uniqueId)
        val targetPlayerConfig = playerConfigRepository.get(targetPlayer.uniqueId)
        if (chatIgnoreRepository.isIgnored(targetPlayerConfig!!.id, playerConfig!!.id)) {
            return Failure(FailureReason.IGNORED(targetPlayerName = targetPlayer.name))
        }

        val tc = TextComponent("✉ [${player.displayName} -> ${targetPlayer.name}] $message").also {
            it.color = ChatColor.GRAY
            it.isItalic = true
        }
        targetPlayer.spigot().sendMessage(tc)
        player.spigot().sendMessage(tc)

        lastWhisperedRepository.set(
            whisperer = player.uniqueId,
            targetOfWhisper = targetPlayer.uniqueId,
        )
        return Success(Unit)
    }
}
