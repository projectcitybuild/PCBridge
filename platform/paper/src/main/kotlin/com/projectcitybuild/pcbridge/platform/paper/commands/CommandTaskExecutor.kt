package com.projectcitybuild.pcbridge.platform.paper.commands

import com.github.shynixn.mccoroutine.bukkit.CoroutineTimings
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.bukkit.plugin.Plugin

fun interface CommandTaskExecutor {
    fun execute(task: suspend () -> Unit)
}

class PluginCommandTaskExecutor(
    private val plugin: Plugin,
) : CommandTaskExecutor {
    override fun execute(task: suspend () -> Unit) {
        plugin.launch(plugin.minecraftDispatcher + object : CoroutineTimings() {}) {
            task()
        }
    }
}

class CoroutineScopeCommandTaskExecutor(
    private val scope: CoroutineScope,
) : CommandTaskExecutor {
    override fun execute(task: suspend () -> Unit) {
        scope.launch {
            task()
        }
    }
}
