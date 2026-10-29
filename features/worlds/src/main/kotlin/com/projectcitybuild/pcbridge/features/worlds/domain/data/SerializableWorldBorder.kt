package com.projectcitybuild.pcbridge.features.worlds.domain.data

import com.google.gson.annotations.JsonAdapter

@JsonAdapter(SerializableWorldBorderTypeAdapter::class)
sealed interface SerializableWorldBorder {
    val centerX: Double
    val centerZ: Double

    data class Square(
        override val centerX: Double,
        override val centerZ: Double,
        val radius: Double,
    ) : SerializableWorldBorder

    data class Circle(
        override val centerX: Double,
        override val centerZ: Double,
        val radius: Double,
    ) : SerializableWorldBorder

    data class Rectangle(
        override val centerX: Double,
        override val centerZ: Double,
        val width: Double,
        val depth: Double,
    ) : SerializableWorldBorder
}
