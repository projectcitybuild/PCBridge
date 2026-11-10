package com.projectcitybuild.pcbridge.features.worlds.domain.data

data class WorldDefinition(
    val registryKey: String,
    val name: String,
    val alias: String?,
    val environment: String?,
    val worldType: String?,
    val seed: Long?,
    val generator: String?,
    val generatorSettings: String?,
    val generateStructures: Boolean?,
    val spawnLocation: WorldSpawnLocation?,
    val scale: Double?,
    val respawnWorld: String?,
    val pvp: Boolean?,
    val properties: Map<String, Any?> = emptyMap(),
)

data class WorldSpawnLocation(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float = 0f,
    val pitch: Float = 0f,
)

data class WorldRegistration(
    val definition: WorldDefinition,
    val isLoaded: Boolean,
)
