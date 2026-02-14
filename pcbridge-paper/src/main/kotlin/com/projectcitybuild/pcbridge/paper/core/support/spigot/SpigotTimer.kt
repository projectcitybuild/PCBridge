package com.projectcitybuild.pcbridge.paper.core.support.spigot

import com.projectcitybuild.pcbridge.paper.core.utils.Cancellable
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import kotlin.time.Duration

// TODO: replace these with coroutines + delay
class SpigotTimer(
    private val plugin: JavaPlugin,
) {
    private val tasks: HashMap<String, BukkitTask> = hashMapOf()

    fun scheduleOnce(
        identifier: String,
        delay: Duration,
        work: () -> Unit,
    ): Cancellable {
        val task = plugin.server.scheduler.runTaskLater(
            plugin,
            work,
            // Times are all in ticks (20 ticks per second)
            delay.inWholeSeconds * 20,
        )
        tasks[identifier] = task

        return Cancellable {
            cancel(identifier = identifier)
        }
    }

    fun scheduleRepeating(
        identifier: String,
        delay: Duration = Duration.ZERO,
        repeatingInterval: Duration,
        work: () -> Unit,
    ): Cancellable {
        val task = plugin.server.scheduler.runTaskTimer(
            plugin,
            work,
            // Times are all in ticks (20 ticks per second)
            delay.inWholeSeconds * 20,
            repeatingInterval.inWholeSeconds * 20,
        )
        tasks[identifier] = task

        return Cancellable {
            cancel(identifier = identifier)
        }
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
