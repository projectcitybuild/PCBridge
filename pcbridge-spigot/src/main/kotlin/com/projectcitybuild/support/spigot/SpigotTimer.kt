package com.projectcitybuild.support.spigot

import com.projectcitybuild.pcbridge.core.contracts.PlatformTimer
import com.projectcitybuild.pcbridge.core.utils.Cancellable
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.TimeUnit

// TODO: replace these with coroutines + delay later
class SpigotTimer(
    private val plugin: JavaPlugin,
) : PlatformTimer {
    private val tasks: HashMap<String, BukkitTask> = hashMapOf()

    override fun scheduleOnce(
        identifier: String,
        delay: Long,
        unit: TimeUnit,
        work: () -> Unit,
    ): Cancellable {
        val task = plugin.server.scheduler.runTaskLater(
            plugin,
            work,
            unit.toSeconds(delay),
        )
        tasks[identifier] = task

        return Cancellable {
            cancel(identifier = identifier)
        }
    }

    override fun scheduleRepeating(
        identifier: String,
        delay: Long,
        repeatingInterval: Long,
        unit: TimeUnit,
        work: () -> Unit,
    ): Cancellable {
        val task = plugin.server.scheduler.runTaskTimer(
            plugin,
            work,
            unit.toSeconds(delay),
            unit.toSeconds(repeatingInterval) * 20, // Expects to be given in ticks (20 ticks per second)
        )
        tasks[identifier] = task

        return Cancellable {
            cancel(identifier = identifier)
        }
    }

    override fun cancel(identifier: String) {
        val task = tasks[identifier]
        if (task != null) {
            task.cancel()
            tasks.remove(identifier)
        }
    }

    override fun cancelAll() {
        plugin.server.scheduler.cancelTasks(plugin)
    }
}
