package com.projectcitybuild.pcbridge.core.remoteconfig

import com.projectcitybuild.pcbridge.http.pcb.models.RemoteConfigVersion

data class RemoteConfigUpdatedEvent(
    val prev: RemoteConfigVersion?,
    val next: RemoteConfigVersion,
)