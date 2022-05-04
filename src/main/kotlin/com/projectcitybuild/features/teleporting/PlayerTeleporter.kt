package com.projectcitybuild.features.teleporting

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.modules.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.events.PlayerPreSummonEvent
import com.projectcitybuild.plugin.events.PlayerPreTeleportEvent
import com.projectcitybuild.repositories.PlayerConfigRepository
import org.bukkit.entity.Player
import javax.inject.Inject

class PlayerTeleporter @Inject constructor(
    private val playerConfigRepository: PlayerConfigRepository,
    private val eventBroadcaster: LocalEventBroadcaster,
) {
    enum class FailureReason {
        TARGET_PLAYER_DISALLOWS_TP,
    }

    fun teleport(
        player: Player,
        destinationPlayer: Player,
        shouldCheckAllowingTP: Boolean,
        shouldSupressTeleportedMessage: Boolean,
    ): Result<Unit, FailureReason> {
        if (shouldCheckAllowingTP) {
            val targetPlayerConfig = playerConfigRepository.get(destinationPlayer.uniqueId)!!
            if (!targetPlayerConfig.isAllowingTPs) {
                return Failure(FailureReason.TARGET_PLAYER_DISALLOWS_TP)
            }
        }

        eventBroadcaster.emit(
            PlayerPreTeleportEvent(player, player.location)
        )

        player.teleport(destinationPlayer)

        player.send().action("Teleported to ${destinationPlayer.name}")

        if (!shouldSupressTeleportedMessage) {
            destinationPlayer.send().action("${player.name} teleported to you")
        }

        return Success(Unit)
    }

    fun summon(
        summonedPlayer: Player,
        destinationPlayer: Player,
        shouldCheckAllowingTP: Boolean,
        shouldSupressTeleportedMessage: Boolean,
    ): Result<Unit, FailureReason> {
        if (shouldCheckAllowingTP) {
            val summonedPlayerConfig = playerConfigRepository.get(summonedPlayer.uniqueId)!!
            if (!summonedPlayerConfig.isAllowingTPs) {
                return Failure(FailureReason.TARGET_PLAYER_DISALLOWS_TP)
            }
        }

        eventBroadcaster.emit(
            PlayerPreSummonEvent(summonedPlayer, summonedPlayer.location)
        )

        summonedPlayer.teleport(destinationPlayer)

        destinationPlayer.send().action("You summoned ${summonedPlayer.name} to you")

        if (!shouldSupressTeleportedMessage) {
            summonedPlayer.send().action("You were summoned to ${destinationPlayer.name}")
        }

        return Success(Unit)
    }
}
