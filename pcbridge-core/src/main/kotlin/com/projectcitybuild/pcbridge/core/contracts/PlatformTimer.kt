package com.projectcitybuild.pcbridge.core.contracts

import com.projectcitybuild.pcbridge.core.utils.Cancellable
import java.util.concurrent.TimeUnit

interface PlatformTimer {
    fun scheduleOnce(
        identifier: String,
        delay: Long,
        unit: TimeUnit,
        work: () -> Unit
    ): Cancellable

    fun scheduleRepeating(
        identifier: String,
        delay: Long,
        repeatingInterval: Long,
        unit: TimeUnit,
        work: () -> Unit
    ): Cancellable

    fun cancel(identifier: String)

    fun cancelAll()
}
