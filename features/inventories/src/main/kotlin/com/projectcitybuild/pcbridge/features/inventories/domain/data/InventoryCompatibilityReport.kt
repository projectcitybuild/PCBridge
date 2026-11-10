package com.projectcitybuild.pcbridge.features.inventories.domain.data

import com.google.gson.JsonObject
import java.io.File

data class InventoryCompatibilityReport(
    val groupConfigurationPresent: Boolean,
    val inventoryConfigurationPresent: Boolean,
    val playerNameMapPresent: Boolean,
    val worldProfiles: Int,
    val groupProfiles: Int,
    val globalProfiles: Int,
    val malformedJsonFiles: List<File>,
    val malformedYamlFiles: List<File>,
)

data class InventoryProfileLocation(
    val directory: String,
    val key: String,
    val playerName: String,
) {
    init {
        require(directory == "worlds" || directory == "groups") {
            "Inventory profiles must use the Multiverse-Inventories worlds or groups directory"
        }
        requireSafePathSegment(key, "profile key")
        requireSafePathSegment(playerName, "player name")
    }
}

enum class InventoryProfileType {
    SURVIVAL,
    CREATIVE,
    ADVENTURE,
    SPECTATOR,
}

internal fun requireSafePathSegment(
    value: String,
    description: String,
) {
    require(
        value.isNotBlank() &&
            value != "." &&
            value != ".." &&
            value.none { it == '/' || it == '\\' || it.isISOControl() } &&
            !value.startsWith('/') &&
            !value.startsWith('\\') &&
            !Regex("^[A-Za-z]:").containsMatchIn(value),
    ) {
        "Invalid $description"
    }
}

internal fun JsonObject.deepCopyObject(): JsonObject = deepCopy()
