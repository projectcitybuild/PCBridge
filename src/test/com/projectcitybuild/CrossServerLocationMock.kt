package com.projectcitybuild

import com.projectcitybuild.entities.CrossServerLocation

fun CrossServerLocationMock(): CrossServerLocation {
    return CrossServerLocation(
        serverName = "server_name",
        worldName = "world_name",
        x = 1.0,
        y = 2.0,
        z = 3.0,
        pitch = 4f,
        yaw = 5f,
    )
}