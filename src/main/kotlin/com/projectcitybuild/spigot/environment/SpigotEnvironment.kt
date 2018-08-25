package com.projectcitybuild.spigot.environment

import com.projectcitybuild.core.protocols.Environment
import com.projectcitybuild.core.services.PlayerStore
import com.projectcitybuild.entities.models.LogLevel
import com.projectcitybuild.entities.models.Player
import java.util.*
import java.util.logging.Logger

class SpigotEnvironment(val logger: Logger, val playerStore: PlayerStore) : Environment {

    override fun log(level: LogLevel, message: String) {
        when (level) {
            LogLevel.VERBOSE -> logger.finest(message)
            LogLevel.DEBUG -> logger.fine(message)
            LogLevel.INFO -> logger.info(message)
            LogLevel.WARNING -> logger.warning(message)
            LogLevel.FATAL -> logger.severe(message)
        }
    }

    override fun get(player: UUID): Player? {
        return playerStore.get(player)
    }

    override fun set(player: Player) {
        playerStore.put(player.uuid, player)
    }

}