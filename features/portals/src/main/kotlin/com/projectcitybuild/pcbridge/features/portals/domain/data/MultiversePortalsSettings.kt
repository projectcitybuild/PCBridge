package com.projectcitybuild.pcbridge.features.portals.domain.data

import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import java.nio.file.Files
import java.nio.file.Path

data class MultiversePortalsSettings(
    val enforcePortalAccess: Boolean = true,
    val sendNoPermissionMessages: Boolean = true,
    val portalsDefaultToNether: Boolean = false,
    val useOnMove: Boolean = true,
    val portalCooldownMillis: Long = 1_000,
    val wandMaterial: Material = Material.WOODEN_PICKAXE,
    val clearOnRemove: Boolean = false,
    val frameMaterials: Set<Material> = emptySet(),
) {
    companion object {
        fun load(file: Path): MultiversePortalsSettings {
            val config = YamlConfiguration()
            if (Files.exists(file)) config.load(file.toFile())
            return MultiversePortalsSettings(
                enforcePortalAccess =
                    config.getBoolean(
                        "portal-usage.enforce-portal-access",
                        config.getBoolean("enforceportalaccess", true),
                    ),
                sendNoPermissionMessages =
                    config.getBoolean("portal-usage.send-no-permission-messages", true),
                portalsDefaultToNether =
                    config.getBoolean(
                        "portal-usage.portals-default-to-nether",
                        config.getBoolean("portalsdefaulttonether", false),
                    ),
                useOnMove =
                    config.getBoolean(
                        "portal-usage.use-on-move",
                        config.getBoolean("useronmove", true),
                    ),
                portalCooldownMillis =
                    config.getLong(
                        "portal-usage.portal-cooldown",
                        config.getLong("portalcooldown", 1_000),
                    ).coerceIn(0, Long.MAX_VALUE / 1_000_000),
                wandMaterial =
                    config.getString("portal-creation.wand-material")
                        ?.uppercase()
                        ?.let { runCatching { Material.valueOf(it) }.getOrNull() }
                        ?: Material.WOODEN_PICKAXE,
                clearOnRemove =
                    config.getBoolean(
                        "portal-creation.clear-on-remove",
                        config.getBoolean("clearonremove", false),
                    ),
                frameMaterials =
                    config.getStringList("portal-creation.frame-materials")
                        .mapNotNull { runCatching { Material.valueOf(it.uppercase()) }.getOrNull() }
                        .toSet(),
            )
        }
    }
}
