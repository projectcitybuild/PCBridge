package com.projectcitybuild.platforms.bungeecord.environment

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.util.concurrent.TimeUnit

class BungeecordTimer(
    private val plugin: Plugin,
    private val proxyServer: ProxyServer
) {
    data class CancellableTask(
        val cancel: () -> Unit
    )

    private val tasks: HashMap<String, ScheduledTask> = hashMapOf()

    fun scheduleRepeating(identifier: String, interval: Long, unit: TimeUnit, work: () -> Unit): CancellableTask {
        cancel(identifier)

        val scheduledTask = proxyServer.scheduler.schedule(plugin, work, 0, interval, unit)
        tasks[identifier] = scheduledTask

        return CancellableTask {
            scheduledTask.cancel()
            tasks.remove(identifier)
        }
    }

    fun cancel(identifier: String) {
        if (tasks.containsKey(identifier)) {
            tasks[identifier].cancel()
            tasks.remove(identifier)
        }
    }

    fun cancelAll() {
        tasks.values.forEach { it.cancel() }
        tasks.clear()
    }
}