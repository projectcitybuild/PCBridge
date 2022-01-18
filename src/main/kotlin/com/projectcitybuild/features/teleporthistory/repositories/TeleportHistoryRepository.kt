package com.projectcitybuild.features.teleporthistory.repositories

import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.TeleportReason
import com.projectcitybuild.modules.database.DataSource
import java.util.*
import javax.inject.Inject

class TeleportHistoryRepository @Inject constructor(
    private val dataSource: DataSource
) {
    fun visit(playerUUID: UUID, location: CrossServerLocation, reason: TeleportReason) {
        
    }
}