package com.projectcitybuild

import com.projectcitybuild.entities.SerializableLocation

fun CrossServerLocationMock(serverName: String = "server_name"): SerializableLocation {
    return SerializableLocation(
        worldName = "world_name",
        x = 1.0,
        y = 2.0,
        z = 3.0,
        pitch = 4f,
        yaw = 5f,
    )
}
