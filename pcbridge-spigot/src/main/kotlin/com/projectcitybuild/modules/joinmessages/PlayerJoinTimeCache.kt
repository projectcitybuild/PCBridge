package com.projectcitybuild.modules.joinmessages

import com.projectcitybuild.pcbridge.core.modules.datetime.time.Time
import java.time.LocalDateTime
import java.util.UUID

class PlayerJoinTimeCache(
    private val time: Time,
) {
    private val joinTimes: MutableMap<UUID, LocalDateTime> = mutableMapOf()

    fun get(uuid: UUID): LocalDateTime? {
        return joinTimes[uuid]
    }

    fun put(uuid: UUID) {
        joinTimes[uuid] = time.now()
    }

    fun remove(uuid: UUID) {
        joinTimes.remove(uuid)
    }
}