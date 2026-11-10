package com.projectcitybuild.pcbridge.features.inventories.domain.data

import org.bukkit.configuration.file.YamlConfiguration
import java.nio.file.Files
import java.nio.file.Path

data class MultiverseInventoryConfig(
    val defaultUngroupedWorlds: Boolean = false,
    val enableGamemodeShareHandling: Boolean = false,
    val useOptionalsForUngroupedWorlds: Boolean = true,
    val activeOptionalShares: Set<String> = emptySet(),
    val useByteSerializationForInventoryData: Boolean = false,
    val alwaysWriteWorldProfile: Boolean = true,
    val applyPlayerDataOnJoin: Boolean = false,
    val applyLastLocationForAllTeleports: Boolean = true,
    val validateBedAnchorRespawnLocation: Boolean = true,
    val inventorySize: Int = 41,
    val enderChestSize: Int = 27,
    val armorSize: Int = 4,
) {
    companion object {
        fun load(file: Path): MultiverseInventoryConfig {
            val config = YamlConfiguration()
            if (Files.exists(file)) config.load(file.toFile())
            return MultiverseInventoryConfig(
                defaultUngroupedWorlds = config.getBoolean(
                    "share-handling.default-ungrouped-worlds",
                    config.getBoolean("default-ungrouped-worlds", false),
                ),
                enableGamemodeShareHandling = config.getBoolean(
                    "share-handling.enable-gamemode-share-handling",
                    config.getBoolean("enable-gamemode-share-handling", false),
                ),
                useOptionalsForUngroupedWorlds = config.getBoolean(
                    "share-handling.use-optionals-for-ungrouped-worlds",
                    config.getBoolean("use-optionals-for-ungrouped-worlds", true),
                ),
                activeOptionalShares = config
                    .getStringList(
                        "share-handling.active-optional-shares",
                    ).ifEmpty { config.getStringList("active-optional-shares") }
                    .toSet(),
                useByteSerializationForInventoryData = config.getBoolean(
                    "sharables.use-byte-serialization-for-inventory-data",
                    config.getBoolean("use-byte-serialization-for-inventory-data", false),
                ),
                alwaysWriteWorldProfile = config.getBoolean(
                    "performance.always-write-world-profile",
                    config.getBoolean("always-write-world-profile", true),
                ),
                applyPlayerDataOnJoin = config.getBoolean(
                    "performance.apply-playerdata-on-join",
                    config.getBoolean("settings.save_load_on_log_in_out", false),
                ),
                applyLastLocationForAllTeleports = config.getBoolean(
                    "sharables.apply-last-location-for-all-teleports",
                    true,
                ),
                validateBedAnchorRespawnLocation = config.getBoolean(
                    "sharables.validate-bed-anchor-respawn-location",
                    true,
                ),
                inventorySize = config.getInt(
                    "misc.max-items-size.inventory",
                    config.getInt("inventory-size", 41),
                ),
                enderChestSize = config.getInt(
                    "misc.max-items-size.ender-chest",
                    config.getInt("ender-chest-size", 27),
                ),
                armorSize = config.getInt(
                    "misc.max-items-size.armor",
                    config.getInt("armor-size", 4),
                ),
            )
        }
    }
}
