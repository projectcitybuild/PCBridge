package com.projectcitybuild.pcbridge.features.inventories.domain.data

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files

enum class InventoryCompatibilityMode {
    READ_ONLY,
    ACTIVE,
}

class InventoryCompatibilitySettings(
    val mode: InventoryCompatibilityMode,
) {
    companion object {
        fun load(file: File): InventoryCompatibilitySettings {
            if (!file.exists()) {
                file.parentFile?.let { Files.createDirectories(it.toPath()) }
                Files.writeString(
                    file.toPath(),
                    """{"mode":"READ_ONLY"}""",
                    UTF_8,
                )
            }

            val root = JsonParser.parseString(Files.readString(file.toPath(), UTF_8))
            require(root.isJsonObject) { "Inventory compatibility settings must be a JSON object" }
            val mode = root.asJsonObject.get("mode")
            require(mode != null && mode.isJsonPrimitive && mode.asJsonPrimitive.isString) {
                "Inventory compatibility settings must define a string mode"
            }
            return InventoryCompatibilitySettings(
                mode =
                    try {
                        InventoryCompatibilityMode.valueOf(mode.asString.uppercase())
                    } catch (failure: IllegalArgumentException) {
                        throw IllegalArgumentException("Unsupported inventory compatibility mode: ${mode.asString}", failure)
                    },
            )
        }
    }
}

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
