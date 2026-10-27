package com.projectcitybuild.pcbridge.app.paperplugin.teleportation

import com.projectcitybuild.pcbridge.features.teleportation.domain.services.RandomTeleportConstraint
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.RandomTeleportConstraintProvider
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.RandomTeleportPosition
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import org.bukkit.Server
import java.util.UUID
import kotlin.random.Random

class WorldBorderRandomTeleportConstraintProvider(
    private val server: Server,
    private val worldBorderRepository: WorldBorderRepository,
) : RandomTeleportConstraintProvider {
    override fun constraintFor(worldId: UUID): RandomTeleportConstraint? {
        val world = server.getWorld(worldId) ?: return null
        val geometry = worldBorderRepository.get(world)?.geometry ?: return null

        return object : RandomTeleportConstraint {
            override fun sample(random: Random): RandomTeleportPosition {
                val (x, z) = geometry.sample(random)
                return RandomTeleportPosition(x, z)
            }

            override fun contains(position: RandomTeleportPosition): Boolean =
                geometry.contains(position.x, position.z)
        }
    }
}
