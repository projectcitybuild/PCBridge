package com.projectcitybuild

import com.projectcitybuild.entities.Warp
import java.time.LocalDateTime

fun WarpMock(name: String = "name"): Warp {
    return Warp(
        name = name,
        location = CrossServerLocationMock(),
        createdAt = LocalDateTime.now(),
    )
}