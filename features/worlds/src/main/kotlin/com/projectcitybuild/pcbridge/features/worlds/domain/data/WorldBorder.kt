package com.projectcitybuild.pcbridge.features.worlds.domain.data

sealed class WorldBorder(
    open val centerX: Double,
    open val centerZ: Double,
) {
    abstract val shape: WorldBorderShape

    data class Square(
        override val centerX: Double,
        override val centerZ: Double,
        val radius: Double,
    ) : WorldBorder(centerX, centerZ) {
        override val shape = WorldBorderShape.SQUARE
    }

    data class Circle(
        override val centerX: Double,
        override val centerZ: Double,
        val radius: Double,
    ) : WorldBorder(centerX, centerZ) {
        override val shape = WorldBorderShape.CIRCLE
    }

    data class Rectangle(
        override val centerX: Double,
        override val centerZ: Double,
        val width: Double,
        val depth: Double,
    ) : WorldBorder(centerX, centerZ) {
        override val shape = WorldBorderShape.RECTANGLE
    }
}
