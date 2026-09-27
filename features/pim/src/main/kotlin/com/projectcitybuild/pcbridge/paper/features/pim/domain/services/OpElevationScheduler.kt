package com.projectcitybuild.pcbridge.paper.features.pim.domain.services

import com.projectcitybuild.pcbridge.paper.core.utils.Cancellable
import com.projectcitybuild.pcbridge.paper.platform.paper.scheduling.SpigotTimer
import java.util.UUID
import kotlin.time.Duration

class OpElevationScheduler(
    private val timer: SpigotTimer,
) {
    private val timers: MutableMap<UUID, Cancellable> = mutableMapOf()

    fun schedule(
        playerUUID: UUID,
        duration: Duration,
        action: suspend () -> Unit,
    ) {
        cancel(playerUUID)
        timers[playerUUID] =
            timer.scheduleOnce(
                identifier = playerUUID.toString(),
                delay = duration,
                work = {
                    timers.remove(playerUUID)
                    action()
                },
            )
    }

    fun cancel(playerUUID: UUID) {
        val job = timers.remove(playerUUID)
        job?.cancel()
    }
}
