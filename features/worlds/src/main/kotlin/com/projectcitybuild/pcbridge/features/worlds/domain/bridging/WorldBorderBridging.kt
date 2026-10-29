package com.projectcitybuild.pcbridge.features.worlds.domain.bridging

import com.projectcitybuild.pcbridge.features.worlds.domain.data.SerializableWorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder

fun SerializableWorldBorder.toWorldBorder(): WorldBorder = when (this) {
    is SerializableWorldBorder.Square -> WorldBorder.Square(centerX, centerZ, radius)
    is SerializableWorldBorder.Circle -> WorldBorder.Circle(centerX, centerZ, radius)
    is SerializableWorldBorder.Rectangle -> WorldBorder.Rectangle(centerX, centerZ, width, depth)
}

fun WorldBorder.toSerializableWorldBorder(): SerializableWorldBorder = when (this) {
    is WorldBorder.Square -> SerializableWorldBorder.Square(centerX, centerZ, radius)
    is WorldBorder.Circle -> SerializableWorldBorder.Circle(centerX, centerZ, radius)
    is WorldBorder.Rectangle -> SerializableWorldBorder.Rectangle(centerX, centerZ, width, depth)
}
