package com.projectcitybuild.spigot.environment

import com.projectcitybuild.api.client.PCBClient
import com.projectcitybuild.core.contracts.Environment
import com.projectcitybuild.core.services.PlayerStore
import com.projectcitybuild.entities.LogLevel
import com.projectcitybuild.entities.models.Player
import com.projectcitybuild.entities.models.PluginConfig
import com.projectcitybuild.entities.models.PluginConfigPair
import net.milkbowl.vault.permission.Permission
import org.bukkit.configuration.file.FileConfiguration
import java.util.*
import java.util.logging.Logger

class SpigotEnvironment(private val logger: Logger,
                        private val playerStore: PlayerStore,
                        private val config: FileConfiguration,
                        private val hooks: SpigotPluginHook) : Environment {

    override fun log(level: LogLevel, message: String) {
        when (level) {
            LogLevel.VERBOSE -> logger.info(message)    // spigot doesn't log FINEST level properly
            LogLevel.DEBUG -> logger.info(message)      // spigot doesn't log FINE level properly
            LogLevel.INFO -> logger.info(message)
            LogLevel.WARNING -> logger.warning(message)
            LogLevel.FATAL -> logger.severe(message)
        }
    }

    override fun get(key: PluginConfigPair): Any {
        return config.get(key.key)
    }

    override fun set(key: PluginConfigPair, value: Any) {
        config.set(key.key, value)
    }

    override fun get(player: UUID): Player? {
        return playerStore.get(player)
    }

    override fun set(player: Player) {
        playerStore.put(player.uuid, player)
    }

    override fun permissions(): Permission? {
        return hooks.permissions
    }


    private var client: PCBClient? = null

    override fun apiClient(): PCBClient {
        if (client == null) {
            val authToken = get(PluginConfig.Api.KEY()) as? String
                    ?: throw Exception("Could not cast auth token to String")

            val baseUrl = get(PluginConfig.Api.BASE_URL()) as? String
                    ?: throw Exception("Could not cast base url to String")

            client = PCBClient(authToken = authToken, baseUrl = baseUrl)
        }
        return client!!
    }

}