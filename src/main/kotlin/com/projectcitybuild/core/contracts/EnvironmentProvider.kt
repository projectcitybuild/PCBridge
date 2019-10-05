package com.projectcitybuild.core.contracts

import com.projectcitybuild.api.client.MojangClient
import com.projectcitybuild.api.client.PCBClient
import com.projectcitybuild.core.utilities.AsyncTask
import com.projectcitybuild.entities.LogLevel
import com.projectcitybuild.entities.Player
import com.projectcitybuild.entities.PluginConfigPair
import me.lucko.luckperms.api.LuckPermsApi
import net.milkbowl.vault.chat.Chat
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

/**
 * The dependency injection layer for each platform (Spigot, Bungee, etc).
 *
 * Each platform has a different way of performing logging, storing configurations,
 * displaying chat colors, etc. Therefore each platform must provide its own
 * specific implementations via an EnvironmentProvider
 *
 */
interface EnvironmentProvider {
    fun get(key: PluginConfigPair) : Any { throw NotImplementedError() }
    fun set(key: PluginConfigPair, value: Any) { throw NotImplementedError() }
    fun log(level: LogLevel, message: String) { println(message) }
    fun get(player: UUID) : Player? { throw NotImplementedError() }
    fun set(player: Player) { throw NotImplementedError() }

    // Runs a given unit of work on a background thread asynchronously
    fun <T> async(task: ((T) -> Unit) -> Unit): AsyncTask<T>

    // Runs a given unit of work on the main thread synchronously
    fun sync(task: () -> Unit)

    val permissions: LuckPermsApi?
        get() = throw NotImplementedError()

    val apiClient: PCBClient
        get() = throw NotImplementedError()

    val mojangClient: MojangClient
        get() = throw NotImplementedError()

    val plugin: JavaPlugin?
        get() = throw NotImplementedError()
}