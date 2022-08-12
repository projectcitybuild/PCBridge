package com.projectcitybuild.support.spigot.timer

import com.projectcitybuild.core.utilities.Cancellable
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SpigotTimer @Inject constructor(
    private val plugin: Plugin,
) : Timer {
    private val tasks: HashMap<String, BukkitTask> = hashMapOf()

    override fun scheduleOnce(
        identifier: String,
        delay: Long,
        unit: TimeUnit,
        work: () -> Unit,
    ): Cancellable {
        val task = plugin.server.scheduler.runTaskLater(plugin, work, unit.toSeconds(delay))
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
        val task = plugin.server.scheduler.runTaskTimer(plugin, work, unit.toSeconds(delay), unit.toSeconds(repeatingInterval))
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
