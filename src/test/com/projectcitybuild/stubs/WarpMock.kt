package com.projectcitybuild

import com.projectcitybuild.entities.SerializableLocation
import com.projectcitybuild.entities.Warp
import java.time.LocalDateTime

fun WarpMock(
    name: String = "name",
    location: SerializableLocation = CrossServerLocationMock()
): Warp {
    return Warp(
        name = name,
        location = location,
        createdAt = LocalDateTime.now(),
    )
}
