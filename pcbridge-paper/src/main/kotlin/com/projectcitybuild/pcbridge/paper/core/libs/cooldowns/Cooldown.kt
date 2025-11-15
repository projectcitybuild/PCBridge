package com.projectcitybuild.pcbridge.paper.core.libs.cooldowns

import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotTimer
import org.bukkit.entity.Player
import kotlin.time.Duration
import kotlin.time.TimeSource

class Cooldown(
    private val timer: SpigotTimer,
) {
    private val timeSource = TimeSource.Monotonic
    private val cooldowns = mutableMapOf<String, TimeSource.Monotonic.ValueTimeMark>()

    fun throttle(duration: Duration, identifier: String) {
        val now = timeSource.markNow()
        val cooldown = cooldowns[identifier]

        if (cooldown != null && cooldown.elapsedNow() < duration) {
            throw CooldownException(
                remainingTime = duration - cooldown.elapsedNow(),
            )
        }

        cooldowns[identifier] = now

        timer.scheduleOnce(
            identifier = identifier,
            delay = duration,
            work = {
                cooldowns.remove(identifier)
                log.debug { "Cooldown expired ($identifier)" }
            }
        )

        log.debug { "Registered cooldown ($identifier). Expires in ${duration.inWholeMilliseconds} ms" }
    }

    fun throttle(
        duration: Duration,
        player: Player,
        identifier: String,
    ) = throttle(
        duration = duration,
        identifier = "${player.uniqueId}_$identifier",
    )
}