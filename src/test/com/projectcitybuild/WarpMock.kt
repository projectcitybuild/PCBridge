package com.projectcitybuild

import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.Warp
import java.time.LocalDateTime

fun WarpMock(
    name: String = "name",
    location: CrossServerLocation = CrossServerLocationMock()
): Warp {
    return Warp(
        name = name,
        location = location,
        createdAt = LocalDateTime.now(),
    )
}