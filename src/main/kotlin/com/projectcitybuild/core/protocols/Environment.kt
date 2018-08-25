package com.projectcitybuild.core.protocols

import com.projectcitybuild.entities.models.LogLevel
import com.projectcitybuild.entities.models.Player
import com.projectcitybuild.entities.models.PluginConfigKey
import java.util.*

interface Environment {
    fun <T>get(key: PluginConfigKey) : T { throw NotImplementedError() }
    fun <T>set(key: PluginConfigKey, value: T) { throw NotImplementedError() }

    fun log(level: LogLevel, message: String) { System.out.println(message) }

    fun get(player: UUID) : Player? { throw NotImplementedError() }
    fun set(player: Player) { throw NotImplementedError() }
}