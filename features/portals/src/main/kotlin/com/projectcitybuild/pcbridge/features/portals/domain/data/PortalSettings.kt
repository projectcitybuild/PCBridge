package com.projectcitybuild.pcbridge.features.portals.domain.data

import org.bukkit.configuration.file.YamlConfiguration
import java.nio.file.Files
import java.nio.file.Path

data class PortalSettings(
    val netherPrefix: String = "",
    val netherSuffix: String = "_nether",
    val endPrefix: String = "",
    val endSuffix: String = "_the_end",
    val handleEndExitRespawn: Boolean = true,
    val bounceBack: Boolean = true,
    val teleportEntities: Boolean = true,
    val sendDisabledPortalMessage: Boolean = true,
    val sendNoDestinationMessage: Boolean = true,
    val endPlatformDropBlocks: Boolean = true,
) {
    companion object {
        fun load(file: Path): PortalSettings {
            val config = YamlConfiguration()
            if (Files.exists(file)) config.load(file.toFile())
            return PortalSettings(
                netherPrefix = config.getString("portal-auto-link-when.nether.prefix", "") ?: "",
                netherSuffix = config.getString("portal-auto-link-when.nether.suffix", "_nether") ?: "_nether",
                endPrefix = config.getString("portal-auto-link-when.end.prefix", "") ?: "",
                endSuffix = config.getString("portal-auto-link-when.end.suffix", "_the_end") ?: "_the_end",
                handleEndExitRespawn = config.getBoolean("handle-end-exit-respawn", true),
                bounceBack = config.getBoolean("bounceback", true),
                teleportEntities = config.getBoolean("teleport-entities", true),
                sendDisabledPortalMessage = config.getBoolean("send-disabled-portal-message", true),
                sendNoDestinationMessage = config.getBoolean("send-no-destination-message", true),
                endPlatformDropBlocks = config.getBoolean("end-platform-drop-blocks", true),
            )
        }
    }
}
