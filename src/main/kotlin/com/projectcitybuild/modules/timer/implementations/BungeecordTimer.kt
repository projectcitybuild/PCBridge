package com.projectcitybuild.modules.timer.implementations

import com.projectcitybuild.core.utilities.Cancellable
import com.projectcitybuild.modules.timer.PlatformTimer
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.util.concurrent.TimeUnit

class BungeecordTimer(
    private val plugin: Plugin,
    private val proxyServer: ProxyServer
): PlatformTimer {
    private val tasks: HashMap<String, ScheduledTask> = hashMapOf()

    override fun scheduleOnce(
        identifier: String,
        delay: Long,
        unit: TimeUnit,
        work: () -> Unit
    ): Cancellable {
        cancel(identifier)

        val scheduledTask = proxyServer.scheduler.schedule(plugin, work, delay, unit)
        tasks[identifier] = scheduledTask

        return Cancellable {
            scheduledTask.cancel()
            tasks.remove(identifier)
        }
    }

    override fun scheduleRepeating(
        identifier: String,
        delay: Long,
        repeatingInterval: Long,
        unit: TimeUnit,
        work: () -> Unit
    ): Cancellable {
        cancel(identifier)

        val scheduledTask = proxyServer.scheduler.schedule(plugin, work, delay, repeatingInterval, unit)
        tasks[identifier] = scheduledTask

        return Cancellable {
            scheduledTask.cancel()
            tasks.remove(identifier)
        }
    }

    override fun cancel(identifier: String) {
        if (tasks.containsKey(identifier)) {
            tasks[identifier]?.cancel()
            tasks.remove(identifier)
        }
    }

    override fun cancelAll() {
        tasks.values.forEach { it.cancel() }
        tasks.clear()
    }
}