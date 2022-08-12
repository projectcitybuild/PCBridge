package com.projectcitybuild.support.spigot.timer

import com.projectcitybuild.core.utilities.Cancellable
import java.util.concurrent.TimeUnit

interface Timer {

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
