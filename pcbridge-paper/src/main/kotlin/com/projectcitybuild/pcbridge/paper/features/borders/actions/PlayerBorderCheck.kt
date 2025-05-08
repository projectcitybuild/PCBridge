package com.projectcitybuild.pcbridge.paper.features.borders.actions

import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.SafeYLocationFinder
import com.projectcitybuild.pcbridge.paper.features.borders.data.Border
import com.projectcitybuild.pcbridge.paper.features.borders.repositories.WorldBorderRepository
import net.kyori.adventure.sound.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.UUID

class PlayerBorderCheck(
    private val worldBorderRepository: WorldBorderRepository,
    private val safeYLocationFinder: SafeYLocationFinder,
) {
    private val processingPlayers: MutableSet<UUID> = mutableSetOf()

    suspend fun moveIfNeeded(player: Player) {
        if (processingPlayers.contains(player.uniqueId)) return

        processingPlayers.add(player.uniqueId)

        log.info { "Checking position of ${player.name}" }

        val location = player.location
        val border = worldBorderRepository.get(location.world)
        if (border != null && !border.contains(player.location)) {
            move(player, border)
        }
        processingPlayers.remove(player.uniqueId)
    }

    private fun move(player: Player, border: Border) {
        val location = player.location
        val clamped = border.clamp(location)

        if (!player.isFlying) {
            clamped.y = safeYLocationFinder.findY(
                world = location.world,
                x = clamped.x.toInt(),
                z = clamped.z.toInt(),
            )?.toDouble() ?: clamped.y
        }

        player.sendRichMessage("<gray><i>You've reached the world border</i></gray>")
        player.teleport(clamped, PlayerTeleportEvent.TeleportCause.PLUGIN)
        player.playSound(
            Sound.sound(
                org.bukkit.Sound.ENTITY_GHAST_SHOOT,
                Sound.Source.NEUTRAL,
                0.7f,
                1.0f,
            )
        )
    }
}