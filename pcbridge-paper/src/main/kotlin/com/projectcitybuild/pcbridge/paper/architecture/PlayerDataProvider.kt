package com.projectcitybuild.pcbridge.paper.architecture

import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import java.net.InetAddress
import java.util.UUID

interface PlayerDataProvider {
    suspend fun get(uuid: UUID, ip: InetAddress?): PlayerData
}