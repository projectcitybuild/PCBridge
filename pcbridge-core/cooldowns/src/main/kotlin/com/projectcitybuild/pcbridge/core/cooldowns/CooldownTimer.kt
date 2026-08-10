package com.projectcitybuild.pcbridge.core.cooldowns

import com.projectcitybuild.pcbridge.shared.utilities.Cancellable
import kotlin.time.Duration

interface CooldownTimer {
    fun scheduleOnce(
        identifier: String,
        delay: Duration,
        work: suspend () -> Unit,
    ): Cancellable
}