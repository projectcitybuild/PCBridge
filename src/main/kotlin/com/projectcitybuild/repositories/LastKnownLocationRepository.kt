package com.projectcitybuild.repositories

import com.projectcitybuild.integrations.essentials.EssentialsIntegration
import org.bukkit.entity.Player
import javax.inject.Inject

class LastKnownLocationRepository @Inject constructor(
    private val essentialsIntegration: EssentialsIntegration,
) {
    fun updateLastKnownLocation(player: Player) {
        essentialsIntegration.updatePlayerLastLocation(player)
    }
}