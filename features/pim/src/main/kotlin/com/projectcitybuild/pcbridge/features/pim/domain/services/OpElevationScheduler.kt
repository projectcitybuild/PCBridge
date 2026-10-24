package com.projectcitybuild.pcbridge.features.pim.domain.services

import com.projectcitybuild.pcbridge.core.utils.Cancellable
import com.projectcitybuild.pcbridge.platform.paper.scheduling.SpigotTimer
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
                identifier = "pim:op-elevation:$playerUUID",
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
