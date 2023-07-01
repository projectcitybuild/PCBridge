package com.projectcitybuild.support.spigot

import com.projectcitybuild.pcbridge.core.utils.AsyncTask
import com.projectcitybuild.pcbridge.core.utils.Cancellable
import com.projectcitybuild.pcbridge.core.contracts.PlatformScheduler
import org.bukkit.plugin.java.JavaPlugin

class SpigotScheduler(private val plugin: JavaPlugin) : PlatformScheduler {

    override fun <T> async(task: ((T) -> Unit) -> Unit): AsyncTask<T> {
        // Bukkit/Spigot performs Asynchronous units of work via their internal Scheduler
        return AsyncTask<T> { resolve ->
            val runnable = Runnable {
                task { result -> resolve(result) }
            }
            val bukkitTask = plugin.server.scheduler.runTaskAsynchronously(plugin, runnable)

            Cancellable {
                bukkitTask.cancel()
            }
        }
    }

    override fun sync(task: () -> Unit) {
        val runnable = Runnable { task() }
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, runnable)
    }
}
