package com.projectcitybuild.spigot.environment

import com.projectcitybuild.core.contracts.Environment
import com.projectcitybuild.core.services.PlayerStore
import com.projectcitybuild.entities.models.LogLevel
import com.projectcitybuild.entities.models.Player
import com.projectcitybuild.entities.models.PluginConfigPair
import org.bukkit.configuration.file.FileConfiguration
import java.util.*
import java.util.logging.Logger

class SpigotEnvironment(val logger: Logger,
                        val playerStore: PlayerStore,
                        val config: FileConfiguration) : Environment {

    override fun log(level: LogLevel, message: String) {
        when (level) {
//            LogLevel.VERBOSE -> logger.finest(message)
//            LogLevel.DEBUG -> logger.fine(message)
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

}