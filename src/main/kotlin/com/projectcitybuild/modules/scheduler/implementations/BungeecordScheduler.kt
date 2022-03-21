package com.projectcitybuild.modules.scheduler.implementations

import com.projectcitybuild.core.utilities.AsyncTask
import com.projectcitybuild.core.utilities.Cancellable
import com.projectcitybuild.modules.scheduler.PlatformScheduler
import net.md_5.bungee.api.plugin.Plugin
import java.util.concurrent.TimeUnit

class BungeecordScheduler(private val plugin: Plugin) : PlatformScheduler {

    override fun <T> async(task: ((T) -> Unit) -> Unit): AsyncTask<T> {
        return AsyncTask<T> { resolve ->
            val runnable = Runnable {
                task { result -> resolve(result) }
            }
            val bukkitTask = plugin.proxy?.scheduler?.runAsync(plugin, runnable)

            Cancellable {
                bukkitTask?.cancel()
            }
        }
    }

    override fun sync(task: () -> Unit) {
        val runnable = Runnable { task() }
        plugin.proxy?.scheduler?.schedule(plugin, runnable, 0, TimeUnit.NANOSECONDS)
    }
}
