package com.projectcitybuild.pcbridge.features.worlds.domain.data

data class SerializableWorldBorder(
    val centerX: Double,
    val centerZ: Double,
    val shape: WorldBorderShape?,
    val radius: Double? = null,
    val width: Double? = null,
    val depth: Double? = null,
)
