package com.projectcitybuild.pcbridge.paper.core.support.spigot

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.projectcitybuild.pcbridge.paper.core.utils.Cancellable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.bukkit.plugin.java.JavaPlugin
import kotlin.time.Duration

class SpigotTimer(
    private val plugin: JavaPlugin,
) {
    private val tasks: HashMap<String, Job> = hashMapOf()
    private val scope = CoroutineScope(plugin.asyncDispatcher)

    fun scheduleOnce(
        identifier: String,
        delay: Duration,
        work: suspend () -> Unit,
    ): Cancellable {
        val job = scope.launch {
            if (delay.isPositive()) {
                delay(delay.inWholeMilliseconds)
            }
            work()
        }
        tasks[identifier] = job
        return Cancellable { cancel(identifier) }
    }

    fun scheduleRepeating(
        identifier: String,
        delay: Duration = Duration.ZERO,
        repeatingInterval: Duration,
        work: suspend () -> Unit,
    ): Cancellable {
        val job = scope.launch {
            if (delay.isPositive()) {
                delay(delay.inWholeMilliseconds)
            }
            while (isActive) {
                work()
                delay(repeatingInterval.inWholeMilliseconds)
            }
        }
        tasks[identifier] = job
        return Cancellable { cancel(identifier) }
    }

    fun cancel(identifier: String) {
        val task = tasks[identifier]
        if (task != null) {
            task.cancel()
            tasks.remove(identifier)
        }
    }

    fun cancelAll() {
        plugin.server.scheduler.cancelTasks(plugin)
    }
}
