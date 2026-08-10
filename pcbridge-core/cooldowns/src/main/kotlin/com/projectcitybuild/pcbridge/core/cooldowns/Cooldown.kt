package com.projectcitybuild.pcbridge.core.cooldowns

import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.TimeSource.Monotonic
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

class Cooldown(
    private val timer: CooldownTimer,
) {
    private val timeSource = Monotonic
    private val cooldowns = mutableMapOf<String, ValueTimeMark>()

    fun throttle(duration: Duration, identifier: String) {
        val now = timeSource.markNow()
        val cooldown = cooldowns[identifier]

        if (cooldown != null && cooldown.elapsedNow() < duration) {
            throw CooldownNotExpiredException(
                remainingTime = duration - cooldown.elapsedNow(),
            )
        }

        cooldowns[identifier] = now

        timer.scheduleOnce(
            identifier = identifier,
            delay = duration,
            work = {
                cooldowns.remove(identifier)
                logSync.debug { "Cooldown expired ($identifier)" }
            }
        )

        logSync.debug { "Registered cooldown ($identifier). Expires in ${duration.inWholeMilliseconds} ms" }
    }

    fun throttle(
        duration: Duration,
        uuid: UUID,
        identifier: String,
    ) = throttle(
        duration = duration,
        identifier = "${uuid}_$identifier",
    )
}