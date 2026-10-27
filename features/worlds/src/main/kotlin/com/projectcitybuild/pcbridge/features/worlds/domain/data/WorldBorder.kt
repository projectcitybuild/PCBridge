package com.projectcitybuild.pcbridge.features.worlds.domain.data

import com.projectcitybuild.pcbridge.features.worlds.domain.geometry.WorldBorderGeometry

sealed class WorldBorder(
    open val centerX: Double,
    open val centerZ: Double,
) {
    val geometry: WorldBorderGeometry by lazy(LazyThreadSafetyMode.PUBLICATION) {
        WorldBorderGeometry(this)
    }

    abstract val shape: WorldBorderShape

    data class Square(
        override val centerX: Double,
        override val centerZ: Double,
        val radius: Double,
    ) : WorldBorder(centerX, centerZ) {
        init {
            validateCenter(centerX, centerZ)
            require(radius.isFinite() && radius > 0 && radius <= WorldBorderLimits.MAX_RADIUS) {
                "Border radius is out of range"
            }
            validateEdges(centerX - radius, centerX + radius, centerZ - radius, centerZ + radius)
        }

        override val shape = WorldBorderShape.SQUARE
    }

    data class Circle(
        override val centerX: Double,
        override val centerZ: Double,
        val radius: Double,
    ) : WorldBorder(centerX, centerZ) {
        init {
            validateCenter(centerX, centerZ)
            require(radius.isFinite() && radius > 0 && radius <= WorldBorderLimits.MAX_RADIUS) {
                "Border radius is out of range"
            }
            validateEdges(centerX - radius, centerX + radius, centerZ - radius, centerZ + radius)
        }

        override val shape = WorldBorderShape.CIRCLE
    }

    data class Rectangle(
        override val centerX: Double,
        override val centerZ: Double,
        val width: Double,
        val depth: Double,
    ) : WorldBorder(centerX, centerZ) {
        init {
            validateCenter(centerX, centerZ)
            require(width.isFinite() && width > 0 && width <= WorldBorderLimits.MAX_DIMENSION) {
                "Rectangle width is out of range"
            }
            require(depth.isFinite() && depth > 0 && depth <= WorldBorderLimits.MAX_DIMENSION) {
                "Rectangle depth is out of range"
            }
            validateEdges(
                centerX - width / 2,
                centerX + width / 2,
                centerZ - depth / 2,
                centerZ + depth / 2,
            )
        }

        override val shape = WorldBorderShape.RECTANGLE
    }

    companion object {
        private fun validateCenter(centerX: Double, centerZ: Double) {
            require(centerX.isFinite() && centerZ.isFinite()) { "Border center must be finite" }
        }

        private fun validateEdges(minX: Double, maxX: Double, minZ: Double, maxZ: Double) {
            val maxRadius = WorldBorderLimits.MAX_RADIUS
            require(minX < maxX && minZ < maxZ) {
                "Border dimensions are too small for the configured center"
            }
            require(minX >= -maxRadius && maxX <= maxRadius && minZ >= -maxRadius && maxZ <= maxRadius) {
                "Border edges are outside the supported world range"
            }
        }
    }
}

object WorldBorderLimits {
    const val MAX_RADIUS = 29_999_984.0
    const val MAX_DIMENSION = MAX_RADIUS * 2
}
