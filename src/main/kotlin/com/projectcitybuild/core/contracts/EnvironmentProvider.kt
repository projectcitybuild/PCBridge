package com.projectcitybuild.core.contracts

import com.projectcitybuild.api.client.MojangClient
import com.projectcitybuild.api.client.PCBClient
import com.projectcitybuild.entities.LogLevel
import com.projectcitybuild.entities.Result
import com.projectcitybuild.entities.models.Player
import com.projectcitybuild.entities.models.PluginConfigPair
import net.milkbowl.vault.chat.Chat
import net.milkbowl.vault.permission.Permission
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
    fun <T> async(completion: (Result<T>) -> Void)

    val permissions: Permission?
        get() = throw NotImplementedError()

    val chat: Chat?
        get() = throw NotImplementedError()

    val apiClient: PCBClient
        get() = throw NotImplementedError()

    val mojangClient: MojangClient
        get() = throw NotImplementedError()

    val plugin: JavaPlugin?
        get() = throw NotImplementedError()
}