package com.projectcitybuild

import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.Warp
import java.time.LocalDateTime

fun WarpMock(name: String = "name"): Warp {
    return Warp(
        name = name,
        location = CrossServerLocation(
            serverName = "server_name",
            worldName = "world_name",
            x = 1.0,
            y = 2.0,
            z = 3.0,
            pitch = 4f,
            yaw = 5f,
        ),
        createdAt = LocalDateTime.now(),
    )
}