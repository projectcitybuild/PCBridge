package com.projectcitybuild.pcbridge.features.worlds.domain.bridging

import com.projectcitybuild.pcbridge.features.worlds.domain.data.SerializableWorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderShape

fun SerializableWorldBorder.toWorldBorder(): WorldBorder {
    val shape = requireNotNull(shape) { "Border shape must be specified." }
    val border = when (shape) {
        WorldBorderShape.SQUARE -> {
            require(width == null && depth == null) {
                "Square borders cannot have width or depth"
            }
            WorldBorder.Square(centerX, centerZ, requireNotNull(radius) { "Border radius must be specified." })
        }
        WorldBorderShape.CIRCLE -> {
            require(width == null && depth == null) {
                "Circular borders cannot have width or depth"
            }
            WorldBorder.Circle(centerX, centerZ, requireNotNull(radius) { "Border radius must be specified." })
        }
        WorldBorderShape.RECTANGLE -> {
            require(radius == null) {
                "Rectangular borders cannot have a radius"
            }
            WorldBorder.Rectangle(
                centerX,
                centerZ,
                requireNotNull(width) {
                    "Rectangle width must be specified."
                },
                requireNotNull(depth) {
                    "Rectangle depth must be specified."
                },
            )
        }
    }
    return border
}

fun WorldBorder.toSerializableWorldBorder(): SerializableWorldBorder = when (this) {
    is WorldBorder.Square -> {
        SerializableWorldBorder(centerX, centerZ, shape, radius = radius)
    }
    is WorldBorder.Circle -> {
        SerializableWorldBorder(centerX, centerZ, shape, radius = radius)
    }
    is WorldBorder.Rectangle -> {
        SerializableWorldBorder(centerX, centerZ, shape, width = width, depth = depth)
    }
}
